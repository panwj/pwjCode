package com.ex.simi.normal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.ex.simi.util.Logv;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

/*
* pHash-like image hash. pHash
* Author: Elliot Shepherd (elliot@jarofworms.com
* Based On: http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
*/
public class ImagePHash {

	private int size = 32;
	private int smallerSize = 8;
	private double[] c;

	public ImagePHash() {
		initCoefficients();
	}

	public ImagePHash(int size, int smallerSize) {
		this.size = size;
		this.smallerSize = smallerSize;
		initCoefficients();
	}

	private void initCoefficients() {
		c = new double[size];
		for (int i = 1; i < size; i++) {
			c[i] = 1;
		}
		c[0] = 1 / Math.sqrt(2.0);
	}

	/**
	 * 汉明距离比较
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int distance(String s1, String s2) {
		int counter = 0;
		for (int k = 0; k < s1.length(); k++) {
			if (s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}

	// Returns a 'binary string' (like. 001010111011100010) which is easy to do
	// a hamming distance on.
	private String calculateFingerPrint(String path) throws Exception {

		/*
		 * 1. Reduce size. Like Average Hash, pHash starts with a small image.
		 * However, the image is larger than 8x8; 32x32 is a good size. This is
		 * really done to simplify the DCT computation and not because it is
		 * needed to reduce the high frequencies.
		 */
		Bitmap bitmap = unifiedBitmap(path, size, size);
		if (bitmap == null || bitmap.isRecycled()) {
			Logv.e("unifiedBitmap() 格式化缩略图失败");
			return null;
		}

		/*
		 * 2. Reduce color. The image is reduced to a grayscale just to further
		 * simplify the number of computations.
		 */
		double[][] vals = getGrayPixels(bitmap, size, size);

		/*
		 * 3. Compute the DCT. The DCT separates the image into a collection of
		 * frequencies and scalars. While JPEG uses an 8x8 DCT, this algorithm
		 * uses a 32x32 DCT.
		 */
		double[][] dctVals = applyDCT(vals);

		/*
		 * 4. Reduce the DCT. This is the magic step. While the DCT is 32x32,
		 * just keep the top-left 8x8. Those represent the lowest frequencies in
		 * the picture.
		 */
		double[][] reduceDctVals = new double[smallerSize][smallerSize];
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				reduceDctVals[x][y] = dctVals[x][y];
			}
		}
		Logv.e("pHash 灰度平均值 1 ： " + getGrayAvg(reduceDctVals));
		/*
		 * 5. Compute the average value. Like the Average Hash, compute the mean
		 * DCT value (using only the 8x8 DCT low-frequency values and excluding
		 * the first term since the DC coefficient can be significantly
		 * different from the other values and will throw off the average).
		 */
		double total = 0;

		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				total += dctVals[x][y];
			}
		}
		total -= dctVals[0][0];

		double avg = total / (double) ((smallerSize * smallerSize) - 1);

		/*
		 * 6. Further reduce the DCT. This is the magic step. Set the 64 hash
		 * bits to 0 or 1 depending on whether each of the 64 DCT values is
		 * above or below the average value. The result doesn't tell us the
		 * actual low frequencies; it just tells us the very-rough relative
		 * scale of the frequencies to the mean. The result will not vary as
		 * long as the overall structure of the image remains the same; this can
		 * survive gamma and color histogram adjustments without a problem.
		 */
		String hash = "";

//		for (int x = 0; x < smallerSize; x++) {
//			for (int y = 0; y < smallerSize; y++) {
//				if (x != 0 && y != 0) {
//					hash += (dctVals[x][y] > avg ? "1" : "0");
//				}
//			}
//		}

		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				hash += (dctVals[x][y] > avg ? "1" : "0");
			}
		}
		Logv.e("pHash hash ： " + hash);

		return hash;
	}

	/**
	 * 统一图片规格
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap unifiedBitmap(String path, int width, int height) {
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		if (bitmap == null) {
			Logv.e("unifiedBitmap() 获取缩略图失败");
			return null;
		}
		float scale_width = (float) width / bitmap.getWidth();
		float scale_height = (float) height / bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scale_width, scale_height);

		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return scaledBitmap;
	}

	private static double[][] getGrayPixels(Bitmap bitmap, int width, int height) {
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

	// DCT function stolen from
	// http://stackoverflow.com/questions/4240490/problems-with-dct-and-idct-algorithm-in-java
	private double[][] applyDCT(double[][] f) {
		int N = size;

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
	 * @param srcPath
	 * @param canPath
	 * @return 值越小相识度越高，10之内可以简单判断这两张图片内容一致
	 * @throws Exception
	 */
	public int isSmailPic(String srcPath, String canPath) throws Exception {
		String imageSrcFile = this.calculateFingerPrint(srcPath);
		String imageCanFile = this.calculateFingerPrint(canPath);
		return this.distance(imageSrcFile, imageCanFile);
	}

}