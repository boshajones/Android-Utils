package com.bosh.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Dimension;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Provides methods to convert between different units or objects, this includes dips and pixels,
 * centimeters to feet, drawables to bitmaps, pounds to kilograms, views to bitmaps, and so on.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class UnitUtils {

	/**
	 * Converts dp unit to equivalent pixels, depending on device density.
	 *
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float dpToPx(@NonNull Context context, @Dimension float dp){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	/**
	 * Converts device specific pixels to density independent pixels.
	 *
	 * @param px A value in px (pixels) unit. Which we need to convert into dp
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float pxToDp(@NonNull Context context, @Px int px){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	public static int sp2px(@NonNull Context context, @Dimension int sp) {
		Resources resources = context.getResources();
		final float fontScale = resources.getDisplayMetrics().scaledDensity;
		return (int) (sp * fontScale + 0.5f);
	}

	public static int px2sp(@NonNull Context context, @Px int px) {
		Resources resources = context.getResources();
		final float fontScale = resources.getDisplayMetrics().scaledDensity;
		return (int) (px / fontScale + 0.5f);
	}

	public static float pxToDimension(final int typedValueUnit, @Px int px, @NonNull DisplayMetrics metrics) {
		switch (typedValueUnit) {
			case TypedValue.COMPLEX_UNIT_PX:
				return px;
			case TypedValue.COMPLEX_UNIT_DIP:
				return px * metrics.density;
			case TypedValue.COMPLEX_UNIT_SP:
				return px * metrics.scaledDensity;
			case TypedValue.COMPLEX_UNIT_PT:
				return px * metrics.xdpi * (1.0f / 72);
			case TypedValue.COMPLEX_UNIT_IN:
				return px * metrics.xdpi;
			case TypedValue.COMPLEX_UNIT_MM:
				return px * metrics.xdpi * (1.0f / 25.4f);
		}
		return 0;
	}

	public static double[] convertCmsToFtIns(double centimeters) {
		double meters = centimeters / 100;

		double feet = meters * 3.28;
		double feetFraction = feet % 1;

		feet = feet - feetFraction;

		double inches = Math.round(feetFraction * 12);

		Log.d("CommonUtil", "convertCmsToFtIns(" + centimeters + ") : Feet = " + feet + ", Inches =" + inches);
		return new double[] {feet, inches};
	}

	public static double convertFtInsToCms(double feet, double inches) {
		if (feet == -1 && inches == -1) {
			return -1;
		}

		double totalInches = feet * 12 + inches;

		double centimeters = totalInches * 2.54;

		Log.d("CommonUtil", "convertFtInsToCms(" + feet + ", " + inches + ") : Centimeters = " + centimeters);
		return centimeters;
	}

	public static double convertKgsToLbs(double kilograms) {
		double lbs = kilograms * 2.2;
		return (double) Math.round(lbs * 100.0) / 100.0;
	}

	public static double convertLbsToKgs(double pounds) {
		double kgs = pounds / 2.2;
		return (double) Math.round(kgs * 100.0) / 100.0;
	}

	public static String bytesToHexString(final byte[] bytes) {
		final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F'};

		if (bytes == null) return null;
		int len = bytes.length;
		if (len <= 0) return null;
		char[] ret = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
			ret[j++] = hexDigits[bytes[i] & 0x0f];
		}
		return new String(ret);
	}

	public static byte[] hexString2Bytes(String hexString) {
		if (StringUtils.isEmpty(hexString)) return null;
		int len = hexString.length();
		if (len % 2 != 0) {
			hexString = "0" + hexString;
			len = len + 1;
		}
		char[] hexBytes = hexString.toUpperCase().toCharArray();
		byte[] ret = new byte[len >> 1];
		for (int i = 0; i < len; i += 2) {
			ret[i >> 1] = (byte) (hexToDec(hexBytes[i]) << 4 | hexToDec(hexBytes[i + 1]));
		}
		return ret;
	}

	private static int hexToDec(final char hexChar) {
		if (hexChar >= '0' && hexChar <= '9') {
			return hexChar - '0';
		} else if (hexChar >= 'A' && hexChar <= 'F') {
			return hexChar - 'A' + 10;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Nullable
	public static String byteToFormattedMemorySize(@IntRange(from=0) long byteNum) {
		if (byteNum < 0) {
			return null;
		} else if (byteNum < MemoryUnit.KB) {
			return String.format(Locale.getDefault(), "%.3fB", (double) byteNum);
		} else if (byteNum < MemoryUnit.MB) {
			return String.format(Locale.getDefault(), "%.3fKB", (double) byteNum / MemoryUnit.KB);
		} else if (byteNum < MemoryUnit.GB) {
			return String.format(Locale.getDefault(), "%.3fMB", (double) byteNum / MemoryUnit.MB);
		} else {
			return String.format(Locale.getDefault(), "%.3fGB", (double) byteNum / MemoryUnit.GB);
		}
	}

	@Nullable
	public static byte[] bitmapToBytes(@Nullable Bitmap bitmap, @Nullable Bitmap.CompressFormat format) {
		if (bitmap == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(format, 100, baos);
		return baos.toByteArray();
	}

	@Nullable
	public static Bitmap bytesToBitmap(final byte[] bytes) {
		return (bytes == null || bytes.length == 0) ? null : BitmapFactory
			.decodeByteArray(bytes, 0, bytes.length);
	}

	@NonNull
	public static Bitmap drawableToBitmap(final Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if (bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}
		Bitmap bitmap;
		if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1,
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		}
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	@Nullable
	public static Drawable bitmapToDrawable(@NonNull Context context, @Nullable Bitmap bitmap) {
		return bitmap == null ? null : new BitmapDrawable(context.getResources(), bitmap);
	}

	@Nullable
	public static byte[] drawableToBytes(@Nullable Drawable drawable, @NonNull Bitmap.CompressFormat format) {
		return drawable == null ? null : bitmapToBytes(drawableToBitmap(drawable), format);
	}

	@Nullable
	public static Drawable bytesToDrawable(@NonNull Context context, final byte[] bytes) {
		return bytes == null ? null : bitmapToDrawable(context, bytesToBitmap(bytes));
	}

	@NonNull
	public static Bitmap viewToBitmap(@NonNull View view) {
		Bitmap ret = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(ret);
		Drawable bgDrawable = view.getBackground();
		if (bgDrawable != null) {
			bgDrawable.draw(canvas);
		} else {
			canvas.drawColor(Color.WHITE);
		}
		view.draw(canvas);
		return ret;
	}

	@NonNull
	public static String stringToMd5(@NonNull String string) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
				.getInstance("MD5");
			digest.update(string.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
				while (h.length() < 2) {
					h.insert(0, "0");
				}
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			UtilLogger.e("UnitUtils", "No such algorithm exception", e);
		}
		return "";
	}
}
