package com.ex.simi.cv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ex.simi.R;
import com.ex.simi.util.Logv;

import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.SIFT;

import java.util.ArrayList;
import java.util.List;

public class ImageCVSIFT {

    public static boolean matchPic(Context context, Bitmap srcBitmap) {
        float nndrRatio = 0.7f;//邻近距离阀值，可自行调整

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher, options);

        Mat tempMat =new Mat();

        Utils.bitmapToMat(bmp1, tempMat);

        Mat srcMat =new Mat();

        Utils.bitmapToMat(srcBitmap, srcMat);

        MatOfKeyPoint templateKeyPoints =new MatOfKeyPoint();

        MatOfKeyPoint srcKeyPoints =new MatOfKeyPoint();

        //初始化SIFT
        SIFT siftDetector = SIFT.create();

        MatOfKeyPoint templateDescriptors =new MatOfKeyPoint();

        MatOfKeyPoint srcDescriptors =new MatOfKeyPoint();

        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

        //获取模板图的特征点
        siftDetector.detect(tempMat, templateKeyPoints);

        siftDetector.detect(srcMat, srcKeyPoints);

        siftDetector.compute(tempMat, templateKeyPoints, templateDescriptors);

        siftDetector.compute(srcMat, srcKeyPoints, srcDescriptors);

        List<MatOfDMatch> matches =new ArrayList<>();

        //获取最佳匹配点列表
        descriptorMatcher.knnMatch(templateDescriptors, srcDescriptors, matches,2);

        int matchCount =0;

        for (int i =0; i < matches.size(); i++) {

            MatOfDMatch match = matches.get(i);

            DMatch[] array = match.toArray();

            DMatch m1 = array[0];

            DMatch m2 = array[1];

            //用邻近距离比值法(NDDR)计算匹配点数
            if (m1.distance <= m2.distance * nndrRatio) {

                ++matchCount;

            }

        }

        Logv.e("======matchCount========" + matchCount);

        if (matchCount >=4 ) {

            //当匹配后的特征点大于等于 4 个，则认为模板图在原图中(这边匹配特征点个数可以根据实际情况自己设置)
            return true;

        }

        return false;
    }
}
