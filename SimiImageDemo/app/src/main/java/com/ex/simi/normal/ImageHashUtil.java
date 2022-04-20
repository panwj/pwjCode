package com.ex.simi.normal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.text.TextUtils;

import com.ex.simi.util.Logv;

public class ImageHashUtil {

    public static final int P_SIZE = 32;
    private static final int P_REDUCE_SIZE = 8;
    public static final int WIDTH = 9;
    public static final int HEIGHT = 8;
    public static final int A_SIZE = 8;
    private static double[] c;
    static {
        c = new double[P_SIZE];
        for (int i = 1; i < P_SIZE; i++) {
            c[i] = 1;
        }
        c[0] = 1 / Math.sqrt(2.0);
    }

    public static void test(String path1, String path2) {
//        String dHash1 = calculateFingerPrintDHash(unifiedBitmap(path1, WIDTH, HEIGHT));
//        String dHash2 = calculateFingerPrintDHash(unifiedBitmap(path2, WIDTH, HEIGHT));
//        Logv.e("dHash: " + (hammingDistance(dHash1, dHash2)));
//
//        String aHash1 = calculateFingerPrintAHash(unifiedBitmap(path1, HEIGHT, HEIGHT));
//        String aHash2 = calculateFingerPrintAHash(unifiedBitmap(path2, HEIGHT, HEIGHT));
//        Logv.e("aHash: " + (hammingDistance(aHash1, aHash2)));
//
//        String pHash1 = calculateFingerPrintPHash(unifiedBitmap(path1, P_SIZE, P_SIZE));
//        String pHash2 = calculateFingerPrintPHash(unifiedBitmap(path2, P_SIZE, P_SIZE));
//        Logv.e("pHash: " + (hammingDistance(pHash1, pHash2)));
    }

    public static long calculateFingerPrintDHash(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            Logv.e("unifiedBitmap() 格式化缩略图失败");
            return 0;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        double[][] grayPixels = getGrayPixels(bitmap, width, height);
        StringBuffer figure = new StringBuffer();
        byte[] bytes = new byte[height * width];
        for (int i = 1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (grayPixels[i][j] >= grayPixels[i - 1][j]) {
                    figure.append(1);
                    bytes[i - 1] = 1;
                } else {
                    figure.append(0);
                    bytes[i - 1] = 0;
                }
            }
        }

        long fingerprint1 = 0;
        long fingerprint2 = 0;
        for (int i = 0; i < 64; i++) {
            if (i < 32) {
                fingerprint1 += (bytes[63 - i] << i);
            } else {
                fingerprint2 += (bytes[63 - i] << (i - 31));
            }
        }

        long finger = (fingerprint2 << 32) + fingerprint1;
        return finger;
    }

    public static long calculateFingerPrintAHash(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            Logv.e("unifiedBitmap() 格式化缩略图失败");
            return 0L;
        }
        double[][] grayPixels = getGrayPixels(bitmap, bitmap.getWidth(), bitmap.getHeight());
        double grayAvg = getGrayAvg(grayPixels);
        long finger = getFingerPrint(grayPixels, grayAvg);
//        Logv.e("aHash finger = " + finger);
        return finger;
    }

    public static long calculateFingerPrintPHash(Bitmap bitmap) {
        /*
         * 1. Reduce size. Like Average Hash, pHash starts with a small image.
         * However, the image is larger than 8x8; 32x32 is a good size. This is
         * really done to simplify the DCT computation and not because it is
         * needed to reduce the high frequencies.
         */
        if (bitmap == null || bitmap.isRecycled()) {
            Logv.e("unifiedBitmap() 格式化缩略图失败");
            return 0L;
        }

        /*
         * 2. Reduce color. The image is reduced to a grayscale just to further
         * simplify the number of computations.
         */
        double[][] grayPixels = getGrayPixels(bitmap, bitmap.getWidth(), bitmap.getHeight());

        /*
         * 3. Compute the DCT. The DCT separates the image into a collection of
         * frequencies and scalars. While JPEG uses an 8x8 DCT, this algorithm
         * uses a 32x32 DCT.
         */
        double[][] dctVals = applyDCT(grayPixels);

        /*
         * 4. Reduce the DCT. This is the magic step. While the DCT is 32x32,
         * just keep the top-left 8x8. Those represent the lowest frequencies in
         * the picture.
         */
        double[][] reduceDctVals = new double[P_REDUCE_SIZE][P_REDUCE_SIZE];
        for (int x = 0; x < P_REDUCE_SIZE; x++) {
            for (int y = 0; y < P_REDUCE_SIZE; y++) {
                reduceDctVals[x][y] = dctVals[x][y];
            }
        }

        /*
         * 5. Compute the average value. Like the Average Hash, compute the mean
         * DCT value (using only the 8x8 DCT low-frequency values and excluding
         * the first term since the DC coefficient can be significantly
         * different from the other values and will throw off the average).
         */
        double grayAvg = getGrayAvg(reduceDctVals);
        Logv.e("pHash 平均灰度值 ： " + grayAvg + "   " + dctVals[0][0]);

        /*
         * 6. Further reduce the DCT. This is the magic step. Set the 64 hash
         * bits to 0 or 1 depending on whether each of the 64 DCT values is
         * above or below the average value. The result doesn't tell us the
         * actual low frequencies; it just tells us the very-rough relative
         * scale of the frequencies to the mean. The result will not vary as
         * long as the overall structure of the image remains the same; this can
         * survive gamma and color histogram adjustments without a problem.
         */
        long finger = getFingerPrint(reduceDctVals, grayAvg);
        return finger;
    }

    /**
     * 转灰度图
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    private static double[][] getGrayPixels(Bitmap bitmap, int width, int height) {
        double[][] pixels = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = computeGrayValue(bitmap.getPixel(i, j));
            }
        }
        return pixels;
    }

    private static double computeGrayValue(int pixel) {
//        int red = (pixel >> 16) & 0xFF;
//        int green = (pixel >> 8) & 0xFF;
//        int blue = (pixel) & 255;
//        return 0.3 * red + 0.59 * green + 0.11 * blue;

        return (((double) ((pixel >> 16) & 255)) * 0.3d) + (((double) ((pixel >> 8) & 255)) * 0.59d) + (((double) (pixel & 255)) * 0.11d);
    }

    /**
     * 计算灰度平均值
     * @param pixels
     * @return
     */
    private static double getGrayAvg(double[][] pixels) {
        int width = pixels[0].length;
        int height = pixels.length;
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                count += pixels[i][j];
            }
        }
//        count -= pixels[0][0];
//        double avg = count / (double) ((width * height) - 1);
        double avg = count / (width * height);
        return avg;
    }

    private static long getFingerPrint(double[][] pixels, double avg) {
        int width = pixels[0].length;
        int height = pixels.length;

        StringBuilder stringBuilder = new StringBuilder();
        byte[] bytes = new byte[height * width];

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

    // DCT function stolen from
    // http://stackoverflow.com/questions/4240490/problems-with-dct-and-idct-algorithm-in-java
    private static double[][] applyDCT(double[][] f) {
        int N = P_SIZE;

        double[][] F = new double[N][N];
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        sum += Math.cos(((2 * i + 1) / (2.0 * N)) * u * Math.PI)
                                * Math.cos(((2 * j + 1) / (2.0 * N)) * v * Math.PI) * (f[i][j]);
                    }
                }
                sum *= ((c[u] * c[v]) / 4.0);
                F[u][v] = sum;
            }
        }
        return F;
    }

    /**
     * 统一图片规格
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap unifiedBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null || bitmap.isRecycled()) {
            Logv.e("unifiedBitmap() 获取缩略图失败");
            return null;
        }
        Bitmap scaledBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
//        float scale_width = (float) width / bitmap.getWidth();
//        float scale_height = (float) height / bitmap.getHeight();
//        Matrix matrix = new Matrix();
//        matrix.postScale(scale_width, scale_height);
//
//        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return scaledBitmap;
    }

    /**
     * 汉明距离比较
     * @param finger1
     * @param finger2
     * @return
     */
    public static int hammingDistance(long finger1, long finger2, String type) {
        if (finger1 == 0 || finger2 == 0) return 100;
        int dist = 0;
        long result = finger1 ^ finger2;
        while (result != 0) {
            ++dist;
            result &= result - 1;
        }
        Logv.e("hammingDistance() dist = " + dist + "    " + type + "   " + result);
        return dist;
    }
}
