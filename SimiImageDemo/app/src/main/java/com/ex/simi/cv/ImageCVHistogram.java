package com.ex.simi.cv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.ex.simi.R;
import com.ex.simi.entry.Group;
import com.ex.simi.entry.Photo;
import com.ex.simi.util.Logv;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* public static void calcHist(List<Mat> images, MatOfInt channels, Mat mask, Mat hist, MatOfInt histSize, MatOfFloat ranges, boolean accumulate)
*
* 参数一：images，待统计直方图的图像数组，数组中所有的图像应具有相同的尺寸和数据类型，并且数据类型只能是CV_8U、CV_16U和CV_32F三种中的一种，但是不同图像的通道数可以不同。
* 参数二：channels，需要统计的通道索引数组，第一个图像的通道索引从0到images[0].channels()-1，第二个图像通道索引从images[0].channels()到images[0].channels()+ images[1].channels()-1，以此类推。
* 参数三：mask，可选的操作掩码，如果是空矩阵则表示图像中所有位置的像素都计入直方图中，如果矩阵不为空，则必须与输入图像尺寸相同且数据类型为CV_8U。
* 参数四：hist，输出的统计直方图结果。
* 参数五：histSize，存放每个维度直方图的数组的尺寸。
* 参数六：ranges，每个图像通道中灰度值的取值范围。
* 参数七：accumulate：是否累积统计直方图的标志，如果累积（true），则统计新图像的直方图时之前图像的统计结果不会被清除，该同能主要用于统计多个图像整体的直方图。
*/

public class ImageCVHistogram {

    private static final float STANDARD_VALUE = 0.12f;//相似阀值，可根据实际情况进行调整

    public void testHistogramMatch(Context context) {
        long time = System.currentTimeMillis();
        Logv.e("testMatch() start create bitmap");
//        Bitmap bmp1 = createBitmap("/storage/emulated/0/ScreenRecord/Screenshot/20220303_175403.jpg");
//        Bitmap bmp2 = createBitmap("/storage/emulated/0/ScreenRecord/Screenshot/20220303_173440.jpg");
        Bitmap bmp1 = createBitmap("/storage/emulated/0/Pictures/图片/BC214994-CFCE-451C-8789-89C3CB5BB154_4_5005_c.jpeg");
        Bitmap bmp2 = createBitmap("/storage/emulated/0/Pictures/图片/207BC41F-ED8A-4394-A13B-8EA9D36BA6AD_1_105_c.jpeg");
        Logv.e("testMatch() end create bitmap");
        if (bmp1 == null || bmp2 == null) return;

        Mat mat1 = calculateMatData(bmp1);
        Mat mat2 = calculateMatData(bmp2);

        comPareHist(mat1, mat2);

        Logv.e("testMatch() 完成一次相似匹配所用时间(毫秒) ：" + (System.currentTimeMillis() - time));
    }

    public List<Group> find(Context context, List<Photo> photos) {
        Logv.e("photos size : " + (photos != null ? photos.size() : 0));
        long time = System.currentTimeMillis();
        for (Photo photo : photos) {
            if (TextUtils.isEmpty(photo.getPath()) || !new File(photo.getPath()).exists()) {
                Logv.e("图片不存在");
                continue;
            }
            Bitmap bitmap = createBitmap(photo.getPath());
            if (bitmap == null) {
                Logv.e("创建bitmap失败");
                continue;
            }
            Mat mat = calculateMatData(bitmap);
            photo.setMat(mat);
        }
        Logv.e("mat --> photos size : " + (photos != null ? photos.size() : 0));

        List<Group> groups = new ArrayList<>();

        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);

            List<Photo> temp = new ArrayList<>();
            temp.add(photo);

            for (int j = i + 1; j < photos.size(); j++) {

                Photo photo2 = photos.get(j);

                boolean dist = comPareHist(photo.getMat(), photo2.getMat());

                if (dist) {
                    temp.add(photo2);
                    photos.remove(photo2);
                    j--;
                }
            }

            Group group = new Group();
            group.setPhotos(temp);
            groups.add(group);
        }

        Logv.e("====================== \n\n " + groups.size());
        Logv.e("opencv直方图判断相似照片耗时 ： " + ((System.currentTimeMillis() - time)/1000));
        return groups;
    }

    /**
     * 获取单通道直方图mat
     * @param bitmap
     * @return
     */
    private static Mat calculateHistData(Bitmap bitmap) {
//        Logv.e("calculateHistData() start... " + bitmap.hashCode());
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap, mat1);

        Mat mat_1 = new Mat();
        //颜色转换
        // TODO: 2022/4/13  Imgproc.COLOR_BGR2HSV可更换，可能影响准确率及效率，需要测试验证
        Imgproc.cvtColor(mat1, mat_1, Imgproc.COLOR_BGR2HSV);
        convertType(mat_1);

        Mat hist_1 = new Mat();
        //颜色范围
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        //直方图大小， 越大匹配越精确 (越慢)
        MatOfInt histSize = new MatOfInt(200);
        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
        recycleBitmap(bitmap);
//        Logv.e("calculateHistData() end... ");
        return hist_1;
    }

    /**
     * 比较RGB通道直方图
     * @param bitmap
     * @return
     */
    private static Mat calculateMatData(Bitmap bitmap) {
//        Logv.e("calculateMatData() start... ");
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap, mat1);

        Mat mat_1 = new Mat();
        //颜色转换
        // TODO: 2022/4/13  Imgproc.COLOR_BGR2GRAY，可能影响准确率及效率，需要测试验证
        Imgproc.cvtColor(mat1, mat_1, Imgproc.COLOR_BGR2GRAY);//COLOR_BGR2HSV
        convertType(mat_1);
        recycleBitmap(bitmap);
//        Logv.e("calculateMatData() end... ");
        return mat_1;
    }

    public static Mat calculateMatData(String path) {
        return calculateHistData(createBitmap(path));
    }

    /**
     * Mat数据格式转换
     * @param srcMat
     * @return
     */
    private static Mat convertType(Mat srcMat) {
        srcMat.convertTo(srcMat, CvType.CV_32F);
        return srcMat;
    }

    public static boolean comPareHist(Mat srcMat, Mat desMat) {
//        Logv.e("comPareHist() start...  " + srcMat + "   " + desMat);
        if (srcMat == null || desMat == null) return false;
        double target = Imgproc.compareHist(srcMat, desMat, Imgproc.CV_COMP_BHATTACHARYYA);
        Logv.e("comPareHist() target : " + target);
        if (target < STANDARD_VALUE) return true;
        return false;
    }

    /**
     * 创建bitmap，width * height、创建方式会影响准确率以及效率
     * @param context
     * @param id
     * @return
     */
    private Bitmap createBitmap(Context context, int id) {
        Logv.e("createBitmap() start...");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id, options);
        if (bmp == null) return null;

        float scale_width = 8.0f / bmp.getWidth();//64.0f --> 获取64位宽像素
        float scale_height = 8.0f / bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width, scale_height);
        Bitmap scaledBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);

        return scaledBitmap;
    }

    /**
     * 创建bitmap，width * height、创建方式会影响准确率以及效率
     * @param path
     * @return
     */
    private static Bitmap createBitmap(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
//        Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), photo.getId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
        Bitmap bmp = BitmapFactory.decodeFile(path, options);

        if (bmp == null) return null;

        float scale_width = 64.0f / bmp.getWidth();//64.0f --> 获取64位宽像素
        float scale_height = 64.0f / bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width, scale_height);
        Bitmap scaledBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        if (scaledBitmap != null && scaledBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            scaledBitmap.setConfig(Bitmap.Config.ARGB_8888);
        }
//        Logv.e("create bitmap : " + (scaledBitmap == null ? null : (scaledBitmap.getWidth() + " * " + scaledBitmap.getHeight())));
        return scaledBitmap;
    }

    private Bitmap createBitmapByOpenCV(String path) {
        Mat mat = Imgcodecs.imread(path, Imgcodecs.IMREAD_UNCHANGED);
        if (mat == null || mat.width() <= 0 || mat.height() <= 0) return null;
//        Logv.e("create bitmap : " + (mat == null ? null : (mat.width() + " * " + mat.height())));
        Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        if (bmp == null) return null;

        float scale_width = 150.0f / bmp.getWidth();//64.0f --> 获取64位宽像素
        float scale_height = 150.0f / bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width, scale_height);
        Bitmap scaledBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        if (scaledBitmap != null && scaledBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            scaledBitmap.setConfig(Bitmap.Config.ARGB_8888);
        }
//        Logv.e("create bitmap : " + (scaledBitmap == null ? null : (scaledBitmap.getWidth() + " * " + scaledBitmap.getHeight())));
        return scaledBitmap;
    }

    private static void recycleBitmap(Bitmap thumb) {
        if(thumb != null && !thumb.isRecycled()){
            thumb.recycle();
            thumb = null;
        }
    }
}
