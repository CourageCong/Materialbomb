/*
 * Copyright (C) 2010 HTC Inc.
 */
package com.fc.materialbomb.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;

import com.fc.materialbomb.FCApplication;
import com.xiaomi.music.materialbomb.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图片工具类
 */
@SuppressWarnings("deprecation")
public class ImageUtil {

	/**
	 * Crops the bitmap to a specific size.
	 * 
	 * @param source
	 *            bitmap to be cropped.
	 * @param targetSize
	 *            target size of the bitmap to be cropped.
	 * @param maxWidth
	 *            maximum width of the bitmap to be cropped.
	 * @return cropped bitmap image.
	 */
	public static Bitmap CropForExtraWidth(Bitmap source, int[] targetSize,
										   int maxWidth) {
		if (source == null || source.isRecycled()) {
			return null;
		}
		if ((source == null) || (targetSize == null)
				|| (targetSize != null && targetSize[0] < maxWidth))
			return source;

		float ratio = (float) source.getWidth() / (float) targetSize[0];

		int targetWidth = (int) (maxWidth * ratio);

		Bitmap newBmp = Bitmap.createBitmap(source,
				(source.getWidth() - targetWidth) / 2, 0, targetWidth,
				source.getHeight());
		if (!source.equals(newBmp))
			source.recycle();

		return newBmp;
	}

	public static Bitmap getMutableBitmap(Bitmap bmp) {
		if (bmp == null || bmp.isRecycled()) {
			return null;
		}
		float scaleFactor = 0.8f;
		Bitmap bitmap = null;
		int width = (int) (bmp.getWidth() * scaleFactor);
		int height = (int) (bmp.getHeight() * scaleFactor);

		byte[] bmpArray = BitmapToBytes(bmp);
		Bitmap output = null;
		int count = 0;
		do {
			count++;
			try {
				bitmap = BitmapFactory.decodeByteArray(bmpArray, 0,
						bmpArray.length);
				output = Bitmap
						.createScaledBitmap(bitmap, width, height, false);
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		} while (output == null && count <= 3);
		if (output != null && output != bmp) {
			if (bmp != null) {
				bmp.recycle();
				bmp = null;
			}
			if (bitmap != null) {
				bitmap.recycle();
				bitmap = null;
			}
			return output;
		}
		return bmp;
	}

	public static Bitmap createInvertedImage(Bitmap source) {
		if (source == null || source.isRecycled()) {
			return null;
		}
		final Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, false);
	}

	/**
	 * Adds a reflection image for each bitmap in film strip view.
	 * 
	 * @param srcBitmap
	 *            Bitmap to be reflected.
	 * @param bkgColor
	 *            background color of reflection image.
	 * @return reflection image for given bitmap.
	 */
	public static Bitmap addReflection(Bitmap srcBitmap, int bkgColor) {
		if (srcBitmap == null || srcBitmap.isRecycled()) {
			return null;
		}
		int reflectionGap = 1;
		final int HEIGHT_REFLECTION = 128;
		final float startAlpha = 105;
		final float offset = startAlpha / HEIGHT_REFLECTION;

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();
		Bitmap outBitmap = Bitmap.createBitmap(width, (height
				+ HEIGHT_REFLECTION + reflectionGap), Config.RGB_565);
		// draw original bitmap
		Canvas canvas = new Canvas(outBitmap);
		canvas.drawColor(bkgColor);
		canvas.drawBitmap(srcBitmap, 0, 0, new Paint(Paint.ANTI_ALIAS_FLAG));

		int[] original = new int[width * HEIGHT_REFLECTION];
		srcBitmap.getPixels(original, 0, width, 0, height - HEIGHT_REFLECTION,
                width, HEIGHT_REFLECTION);

		int reflection[] = new int[width * HEIGHT_REFLECTION];

		for (int i = 0; i < HEIGHT_REFLECTION; i++) {
			int alpha = (int) (startAlpha - offset * i);
			for (int j = 0; j < width; j++) {
				int sourcePixel = original[(HEIGHT_REFLECTION - i - 1) * width
						+ j];
				reflection[i * width + j] = Color.argb((int) alpha, // Alpha
						Color.red(sourcePixel), // Red
						Color.green(sourcePixel), // Green
						Color.blue(sourcePixel)); // Blue
			}
		}
		// draw reflection
		Bitmap reflectionBmp = Bitmap.createBitmap(reflection, width,
				HEIGHT_REFLECTION, Config.ARGB_8888);
		canvas.drawBitmap(reflectionBmp, 0, height + reflectionGap, new Paint(
				Paint.ANTI_ALIAS_FLAG));

		srcBitmap.recycle();
		srcBitmap = null;
		reflectionBmp.recycle();
		reflectionBmp = null;

		return outBitmap;
	}

	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

		return bitmapWithReflection;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap getGrayScaleBitmap(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(output);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(filter);
		c.drawBitmap(bitmap, 0, 0, paint);
		return output;
	}

	public static Bitmap getCircleBitmap(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap == null) {
            File file = new File(path);
            if(file.exists() && file.length() ==0)
                file.delete();
            return BitmapFactory.decodeResource(FCApplication.getInstance().getResources(), R.mipmap.avatar_iconfinder_default);
        }
		return getCircleBitmap(bitmap);
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap) {
		return getCircleBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap, int width, int height) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		Bitmap croppedBitmap = scaleCenterCrop(bitmap, width, height);
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();

		final Rect rect = new Rect(0, 0, width, height);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);

		int radius = 0;
		if (width > height) {
			radius = height / 2;
		} else {
			radius = width / 2;
		}

		canvas.drawCircle(width / 2, height / 2, radius, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(croppedBitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap scaleCenterCrop(Bitmap source, int newHeight,
										 int newWidth) {
		if (source == null || source.isRecycled()) {
			return null;
		}
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();

		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);

		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;

		float left = (newWidth - scaledWidth) / 2;
		float top = (newHeight - scaledHeight) / 2;

		RectF targetRect = new RectF(left, top, left + scaledWidth, top
				+ scaledHeight);

		Bitmap dest = Bitmap.createBitmap(newWidth, newHeight,
				source.getConfig());
		Canvas canvas = new Canvas(dest);
		canvas.drawBitmap(source, null, targetRect, null);

		return dest;
	}

	/**
	 * Calculates the new size of image.
	 * 
	 * @param width
	 *            width of image.
	 * @param height
	 *            height of image.
	 * @param maxHeight
	 *            maximum height of image.
	 * @return calculated new size.
	 */
	public static int[] countNewSize(int width, int height, int maxHeight) {
		int[] newSize = new int[2];

		newSize[0] = 1;
		if (height != 0)
			newSize[0] = width * maxHeight / height;

		if (newSize[0] <= 0)
			newSize[0] = 1;
		newSize[1] = maxHeight;
		return newSize;
	}

	public static Bitmap resize(Bitmap source, float scaleFactor) {
		if (source == null || source.isRecycled()) {
			return null;
		}
		if (0.04 < scaleFactor && scaleFactor < 0.96) {
			int originalWidth = source.getWidth();
			int orginalHeight = source.getHeight();
			return resize(source, (int) (originalWidth * scaleFactor),
					(int) (orginalHeight * scaleFactor));
		}
		return source;
	}

	/**
	 * Resizes the bitmap image.
	 * 
	 * @param source
	 *            bitmap to be resized.
	 * @param newWidth
	 *            new width of bitmap to be resized.
	 * @param newHeight
	 *            new height of bitmap to be resized.
	 * @return
	 */
	public static Bitmap resize(Bitmap source, int newWidth, int newHeight) {
		if (source == null || source.isRecycled()) {
			return null;
		}

		Matrix matrix = new Matrix();
		int originalWidth = source.getWidth();
		int orginalHeight = source.getHeight();
		matrix.postScale(((float) newWidth / originalWidth),
				((float) newHeight / orginalHeight));

		Bitmap output = null;
		int count = 0;
		do {
			count++;
			try {
				output = Bitmap.createBitmap(source, 0, 0, originalWidth,
						orginalHeight, matrix, false);
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		} while (output == null && count <= 3);
		if (output != null && !source.equals(output)) {
			source.recycle();
		}
		return output;
	}

	public static Bitmap rotateAndScale(Bitmap source, int degrees, float scale) {
		if (source == null || source.isRecycled()) {
			return null;
		}

		Matrix matrix = new Matrix();
		if (degrees > 0) {
			matrix.setRotate(degrees);
		}
		if (Float.compare(scale, 1f) != 0) {
			matrix.setScale(scale, scale);
		}

		Bitmap output = null;
		int count = 0;
		do {
			count++;
			try {
				output = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
						source.getHeight(), matrix, true);
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		} while (output == null && count <= 3);

		if (output != source) {
			source.recycle();
			source = null;
		}
		return output;
	}

	/**
	 * get support image size.
	 * 
	 * @param context
	 * @return
	 */
	public static int scaleFromDensity(Context context) {
		int size = -1;
		if (context == null) {
			return size;
		}

		int deviceDensity = context.getResources().getDisplayMetrics().densityDpi;
		if (DisplayMetrics.DENSITY_HIGH <= deviceDensity) {
			size = DisplayMetrics.DENSITY_HIGH;
        } else if (DisplayMetrics.DENSITY_MEDIUM <= deviceDensity) {
            size = DisplayMetrics.DENSITY_MEDIUM;
        } else {
            size = deviceDensity;
		}

		return size;
	}

	/**
	 * resize the bitmap to fit width size.
	 * 
	 * @param source
	 *            bitmap to be scaled.
	 * @param targetWidth
	 *            target width image size of the bitmap to be scaled.
	 * @return scaled bitmap image.
	 */
	public static Bitmap ScaleFitX(Bitmap source, int targetWidth) {
		if (source == null || targetWidth == 0) {
			return source;
		}
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int targetHeight = targetWidth * sourceHeight / sourceWidth;
		if (targetHeight < 0) {
			return source;
		}

		return resize(source, targetWidth, targetHeight);
	}

	public static InputStream getStreamFromDrawableId(Context ctx,
													  int drawableId) {
		BitmapDrawable drawable = (BitmapDrawable) ctx.getResources()
				.getDrawable(drawableId);
		Bitmap bitmap = drawable.getBitmap();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, os);
		return new ByteArrayInputStream(os.toByteArray());
	}

	public static Bitmap getBitmapFromDrawableId(Context ctx, int drawableId) {
		BitmapDrawable drawable = (BitmapDrawable) ctx.getResources()
				.getDrawable(drawableId);
		return drawable.getBitmap();
	}

	/**
	 * Convert the drawable to the Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
								: Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		return new BitmapDrawable(bitmap);
	}

	public static Bitmap convertViewToBitmap(View v) {
		if (v == null || v.getVisibility() == View.GONE) {
			return null;
		}
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
		v.buildDrawingCache();
		Bitmap bitmap = v.getDrawingCache();
		return bitmap;
	}

	/**
	 * Convert the byte array to the Bitmap
	 * 
	 * @param bytes
	 * @return
	 */
	public static Bitmap BytesToBitmap(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError ex) {
			System.gc();
			ex.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * Convert the bitmap to the byte array
	 * 
	 * @param bitmap
	 * @return
	 */
	public static byte[] BitmapToBytes(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 80, baos);
		return baos.toByteArray();
	}


	@SuppressWarnings("deprecation")
	protected static Drawable bytesToDrawable(byte[] data) {
		if (data == null) {
			return null;
		}
		int MAX_SIZE = 256;
		BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
		sizeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, sizeOptions);

		int scale = 1;
		int width_tmp = sizeOptions.outWidth;
		int height_tmp = sizeOptions.outHeight;
		while (width_tmp / 2 >= MAX_SIZE && height_tmp / 2 >= MAX_SIZE) {
			scale *= 2;
			width_tmp /= 2;
			height_tmp /= 2;
		}
		BitmapFactory.Options resultOptions = new BitmapFactory.Options();
		resultOptions.inSampleSize = scale;
		Bitmap bitmap = ImageUtil.getRoundedCornerBitmap(BitmapFactory
				.decodeByteArray(data, 0, data.length, resultOptions), 8);
		return new BitmapDrawable(bitmap);
	}

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	public static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static boolean saveBitmapToFile(Bitmap bmp, String fileName) {
		if (bmp == null || bmp.isRecycled()) {
			return false;
		}
		if (fileName == null || fileName.length() == 0) {
			return false;
		}

		File file = new File(fileName);
		if (file != null && !file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		CompressFormat format = CompressFormat.PNG;
		int quality = 100;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}

	public static Bitmap decodeFile(String pathName, boolean mutable) {
		Bitmap bmp = decodeFile(pathName);
		if (mutable) {
			bmp = getMutableBitmap(bmp);
		}
		return bmp;
	}

	public static Bitmap decodeFile(String pathName) {
		final int MAX_SINGLE_IMAGE_PIX = 1280 * 1280;

		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false;
		bfOptions.inPurgeable = true;
		bfOptions.inTempStorage = new byte[12 * 1024];
		File file = new File(pathName);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Bitmap bmp = null;
		if (fs != null) {
			try {
				bfOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(fs, null, bfOptions);
				fs.close();
				bfOptions.inSampleSize = computeSampleSize(bfOptions, -1,
						MAX_SINGLE_IMAGE_PIX);
				bfOptions.inJustDecodeBounds = false;
				bmp = BitmapFactory.decodeFile(pathName, bfOptions);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
			} finally {
				if (fs != null) {
					try {
						fs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return bmp;
	}

	public static Bitmap decodeStream(InputStream is) {
		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false;
		bfOptions.inPurgeable = true;
		bfOptions.inTempStorage = new byte[12 * 1024];

		Bitmap bmp = null;
		if (is != null) {
			try {
				bfOptions.inJustDecodeBounds = false;

				bmp = BitmapFactory.decodeStream(is, null, bfOptions);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return bmp;
	}

	public static Bitmap bitmapAddIcon(Bitmap bitmap, Bitmap icon, int iconSize) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		if (icon == null || icon.isRecycled()) {
			return null;
		}
		Bitmap contactIcon = Bitmap.createBitmap(iconSize, iconSize,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(contactIcon);

		Paint iconPaint = new Paint();
		iconPaint.setDither(true);
		iconPaint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Rect dst = new Rect(0, 0, iconSize, iconSize);
		canvas.drawBitmap(bitmap, src, dst, iconPaint);

		Rect src2 = new Rect(0, 0, icon.getWidth(), icon.getHeight());
		Rect dst2 = new Rect(iconSize / 4, iconSize / 4, iconSize * 3 / 4,
				iconSize * 3 / 4);
		canvas.drawBitmap(icon, src2, dst2, iconPaint);

		return contactIcon;
	}
	


	public static Bitmap getRoundedCornerBitmap(Context ctx, int drawableId,
												float roundPx) {
		BitmapDrawable drawable = (BitmapDrawable) ctx.getResources()
				.getDrawable(drawableId);
		return getRoundedCornerBitmap(drawable, roundPx);
	}

	public static Bitmap getRoundedCornerBitmap(BitmapDrawable drawable,
												float roundPx) {
		if (drawable == null) {
			return null;
		}
		return getRoundedCornerBitmap(drawable.getBitmap(), roundPx);
	}

	public static Drawable getRoundedCornerDrawable(BitmapDrawable drawable,
													float roundPx) {
		if (drawable == null) {
			return null;
		}
		return new BitmapDrawable(getRoundedCornerBitmap(drawable.getBitmap(),
				roundPx));
	}


	/**
	 * resize the bitmap to fit height size.
	 * 
	 * @param source
	 *            bitmap to be scaled.
	 * @param targetHeight
	 *            target height image size of the bitmap to be scaled.
	 * @return scaled bitmap image.
	 */
	public static Bitmap ScaleFitY(Bitmap source, int targetHeight) {
		if (source == null || targetHeight == 0) {
			return source;
		}
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int targetWidth = targetHeight * sourceWidth / sourceHeight;
		if (targetWidth < 0) {
			return source;
		}

		return resize(source, targetWidth, targetHeight);
	}
	/**
	 * @Description: 高斯模糊图片
	 * @param    bitmap context
	 * @return   bitmap
	 * @throws
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Bitmap blurBitmap(Bitmap bitmap, Context context){

		//Let's create an empty bitmap with the same size of the bitmap we want to blur
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);

		//Instantiate a new Renderscript
		RenderScript rs = RenderScript.create(context);

		//Create an Intrinsic Blur Script using the Renderscript
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

		//Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
		Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
		Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

		//Set the radius of the blur
		blurScript.setRadius(25.f);

		//Perform the Renderscript
		blurScript.setInput(allIn);
		blurScript.forEach(allOut);

		//Copy the final bitmap created by the out Allocation to the outBitmap
		allOut.copyTo(outBitmap);

		//recycle the original bitmap
		bitmap.recycle();

		//After finishing everything, we destroy the Renderscript.
		rs.destroy();

		return outBitmap;


	}



}
