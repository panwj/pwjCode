package com.ex.simi.normal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.ex.simi.entry.Group;
import com.ex.simi.entry.Photo;
import com.ex.simi.util.BitmapUtil;
import com.ex.simi.util.Logv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageAHash {

    public static List<Group> find(Context context, List<Photo> photos) {
        calculateFingerPrint(context, photos);

        List<Group> groups = new ArrayList<>();

        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);

            List<Photo> temp = new ArrayList<>();
            temp.add(photo);

            for (int j = i + 1; j < photos.size(); j++) {

                Photo photo2 = photos.get(j);

                int dist = hamDist(photo.getFinger(), photo2.getFinger());

                if (dist < 5) {
                    temp.add(photo2);
                    photos.remove(photo2);
                    j--;
                }
            }

            Group group = new Group();
            group.setPhotos(temp);
            groups.add(group);
        }
        Logv.e("aHash groups : " + groups.size());

        return groups;
    }

    private static void calculateFingerPrint(Context context, List<Photo> photos) {
        float scale_width, scale_height;

        for (Photo p : photos) {
            if (TextUtils.isEmpty(p.getPath()) || !new File(p.getPath()).exists()) {
                Logv.e("calculateFingerPrint() 文件不存在 : " + p.getPath());
                continue;
            }
            /**
             * 首次创建系统缩略图相当耗时
             * 通过path读取bitmap比较快
             */
//            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), p.getId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
            Bitmap bitmap = BitmapFactory.decodeFile(p.getPath());
            if (bitmap == null) {
                Logv.e("calculateFingerPrint() 获取缩略图失败");
                continue;
            }
            scale_width = 8.0f / bitmap.getWidth();
            scale_height = 8.0f / bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(scale_width, scale_height);

            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            p.setFinger(getFingerPrint(scaledBitmap));

            bitmap.recycle();
            scaledBitmap.recycle();
        }
    }

    private static long getFingerPrint(Bitmap bitmap) {
        double[][] grayPixels = getGrayPixels(bitmap);
        double grayAvg = getGrayAvg(grayPixels);
        return getFingerPrint(grayPixels, grayAvg);
    }

    private static long getFingerPrint(double[][] pixels, double avg) {
        int width = pixels[0].length;
        int height = pixels.length;

        byte[] bytes = new byte[height * width];

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (pixels[i][j] >= avg) {
                    bytes[i * height + j] = 1;
                    stringBuilder.append("1");
                } else {
                    bytes[i * height + j] = 0;
                    stringBuilder.append("0");
                }
            }
        }

//        Logv.e("getFingerPrint: " + stringBuilder.toString());

        long fingerprint1 = 0;
        long fingerprint2 = 0;
        for (int i = 0; i < 64; i++) {
            if (i < 32) {
                fingerprint1 += (bytes[63 - i] << i);
            } else {
                fingerprint2 += (bytes[63 - i] << (i - 31));
            }
        }

        return (fingerprint2 << 32) + fingerprint1;
    }

    private static double getGrayAvg(double[][] pixels) {
        int width = pixels[0].length;
        int height = pixels.length;
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                count += pixels[i][j];
            }
        }
        return count / (width * height);
    }


    private static double[][] getGrayPixels(Bitmap bitmap) {
        int width = 8;
        int height = 8;
        double[][] pixels = new double[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = computeGrayValue(bitmap.getPixel(i, j));
            }
        }
        return pixels;
    }

    private static double computeGrayValue(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = (pixel) & 255;
        return 0.3 * red + 0.59 * green + 0.11 * blue;
    }

    private static int hamDist(long finger1, long finger2) {
        int dist = 0;
        long result = finger1 ^ finger2;
        while (result != 0) {
            ++dist;
            result &= result - 1;
        }
        return dist;
    }
}
