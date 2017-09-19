package com.bosh.utils;

import android.Manifest.permission;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Provides methods related to images and image manipulation including; tinting drawables, compressing
 * bitmaps, and creating thumbnails. In addition there are several gallery methods including the
 * ability to add an Image File to the Media Store and retrieving the users recent images.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ImageUtils {

	/**
	 * Method to tint a drawable to a specific colour, this ensures the original and tinted drawables
	 * have been mutated so that the state is not shared with any other instance of that drawable.
	 *
	 * @param context   {@link Context}
	 * @param drawableResId     Drawable Resource ID for the Drawable to tint
	 * @param colorResId    Color Resource ID for the color to tint the drawable to
	 * @return  The tinted {@link Drawable} instance
	 */
	public static Drawable getTintedDrawable(Context context, @DrawableRes int drawableResId, @ColorRes int colorResId) {
		Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
		int color = ContextCompat.getColor(context, colorResId);

		return getTintedDrawable(drawable, color);
	}

	/**
	 * Method to tint a drawable to a specific colour, this ensures the original and tinted drawables
	 * have been mutated so that the state is not shared with any other instance of that drawable.
	 *
	 * @param drawable  {@link Drawable} object to tint
	 * @param color Color value to tint the drawable to
	 * @return  The tinted {@link Drawable} instance
	 */
	public static Drawable getTintedDrawable(Drawable drawable, @ColorInt int color) {
		Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
		wrappedDrawable = wrappedDrawable.mutate();
		DrawableCompat.setTint(wrappedDrawable, color);

		return wrappedDrawable;
	}

	/**
	 * Creates and returns a {@link File} object which contains a compressed version of the image provided
	 * by the {@link Uri}. This image is compressed based on default values set within this method, while the
	 * method handles {@link OutOfMemoryError}, Orientation, and maintaining aspect ratio.
	 *
	 * This implementation will, by default, create a compressed JPEG image with decent quality at a
	 * 4:3 ratio. Most respectably sized images will result in a compressed size of sub 100kb.
	 *
	 * The compressed image file is stored in the Applications internal cache directory, which can be
	 * modified in {@link ImageUtils#createCompressedImageFile(Context, CompressFormat)} and can also
	 * be cleaned up once used with the {@link FileUtils#cleanUpCache(Context)} method.
	 *
	 * @param context	{@link Context}
	 * @param filepath	{@link String} File path reference to the original image
	 * @return	{@link File} containing the abstract pathname of the compressed image, or null
	 */
	@Nullable
	@SuppressWarnings("MissingPermission")
	@RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
	public static File compressImage(@NonNull Context context, @NonNull String filepath) {
		final float COMPRESSION_MAX_WIDTH = 816;
		final float COMPRESSION_MAX_HEIGHT = 612;
		final int COMPRESSION_QUALITY = 80;

		return compressImage(context, filepath, COMPRESSION_MAX_WIDTH,
			COMPRESSION_MAX_HEIGHT, COMPRESSION_QUALITY, CompressFormat.JPEG);
	}

	/**
	 * Creates and returns a {@link File} object which contains a compressed version of the image provided
	 * by the {@link Uri}. This image is compressed based on the other parameters provided, while the
	 * method handles {@link OutOfMemoryError}, Orientation, and maintaining aspect ratio.
	 *
	 * The compressed image file is stored in the Applications internal cache directory, which can be
	 * modified in {@link ImageUtils#createCompressedImageFile(Context, CompressFormat)} and can also
	 * be cleaned up once used with the {@link FileUtils#cleanUpCache(Context)} method.
	 *
	 * @param context	{@link Context}
	 * @param imageUri	{@link Uri} reference to the original image
	 * @param maxWidth	Maximum width of the compressed image
	 * @param maxHeight	Maximum height of the compressed image
	 * @param quality	Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
	 * 					compress for max quality. Some formats, like PNG which is lossless, will
	 * 					ignore the quality setting
	 * @param compressFormat	The format of the compressed image
	 * @return	{@link File} containing the abstract pathname of the compressed image, or null
	 */
	@Nullable
	@SuppressWarnings("MissingPermission")
	@RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
	public static File compressImage(@NonNull Context context, @NonNull Uri imageUri, float maxWidth,
			float maxHeight, @IntRange(from=1,to=100) int quality, @NonNull CompressFormat compressFormat) {

		String filePath = FileUtils.getFilePath(context, imageUri);
		if (filePath != null) {
			return compressImage(context, filePath, maxWidth, maxHeight, quality, compressFormat);
		}
		return null;
	}

	/**
	 * Creates and returns a {@link File} object which contains a compressed version of the image provided
	 * by the absolute file path. This image is compressed based on the other parameters provided, while the
	 * method handles {@link OutOfMemoryError}, Orientation, and maintaining aspect ratio.
	 *
	 * The compressed image file is stored in the Applications internal cache directory, which can be
	 * modified in {@link ImageUtils#createCompressedImageFile(Context, CompressFormat)} and can also
	 * be cleaned up once used with the {@link FileUtils#cleanUpCache(Context)} method.
	 *
	 * @param context	{@link Context}
	 * @param filePath	Absolute file path of the original image
	 * @param maxWidth	Maximum width of the compressed image
	 * @param maxHeight	Maximum height of the compressed image
	 * @param quality	Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
	 * 					compress for max quality. Some formats, like PNG which is lossless, will
	 * 					ignore the quality setting
	 * @param compressFormat	The format of the compressed image
	 * @return	{@link File} containing the abstract pathname of the compressed image, or null
	 */
	@Nullable
	@SuppressWarnings("MissingPermission")
	@RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
	public static File compressImage(@NonNull Context context, @NonNull String filePath, float maxWidth,
			float maxHeight, @IntRange(from=1,to=100) int quality, @NonNull CompressFormat compressFormat) {

		Bitmap scaledBitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();

		// Ensure inJustDecodeBounds is set to true as this prevents the bitmap pixels from being loaded
		// into memory and just loads the bounds. However attempting to use the bitmap will return null
		options.inJustDecodeBounds = true;

		String path = new File(filePath).getAbsolutePath();
		Bitmap bmp = BitmapFactory.decodeFile(path, options);

		int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;

		if (actualHeight == 0 || actualWidth == 0) {
			return null;
		}

		// Calculate the ratio of the current image, and the ratio of the outputted image
		float imgRatio = actualWidth / actualHeight;
		float maxRatio = maxWidth / maxHeight;

		// Defines the height and width of the compressed image with respect to the ratio
		if (actualHeight > maxHeight || actualWidth > maxWidth) {
			if (imgRatio < maxRatio) {
				imgRatio = maxHeight / actualHeight;
				actualWidth = (int) (imgRatio * actualWidth);
				actualHeight = (int) maxHeight;
			} else if (imgRatio > maxRatio) {
				imgRatio = maxWidth / actualWidth;
				actualHeight = (int) (imgRatio * actualHeight);
				actualWidth = (int) maxWidth;
			} else {
				actualHeight = (int) maxHeight;
				actualWidth = (int) maxWidth;
			}
		}

		// Define a sample size which will load in a scaled down version of the original image
		options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

		// inJustDecodeBounds can now be set to false to load the actual bitmap
		options.inJustDecodeBounds = false;

		// Mostly for older Android versions, these options allow android to claim the bitmap memory if it runs low on memory
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16 * 1024];

		try {
			// Load the bitmap from its file path and create the scaled bitmap
			bmp = BitmapFactory.decodeFile(filePath, options);
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
		} catch (OutOfMemoryError exception) {
			UtilLogger.e("ImageUtils", "compressImage(" + filePath + ", " + maxWidth + ", " + maxHeight + ", " + quality + ", "
				+ compressFormat.toString() + ") Out of memory exception decoding bitmap file or creating scaled bitmap");
		}

		if (scaledBitmap == null) {
			UtilLogger.e("ImageUtils", "compressImage(" + filePath + ", " + maxWidth + ", " + maxHeight + ", " + quality + ", "
				+ compressFormat.toString() + ") Unable to compress image");
			return null;
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float) options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(
			Paint.FILTER_BITMAP_FLAG));

		//Use the exif data of the image to ensure it is rotated to the correct orientation
		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(),
				scaledBitmap.getHeight(), matrix, true);
		} catch (IOException ioe) {
			UtilLogger.d("ImageUtils", "compressImage(" + filePath + ", " + maxWidth + ", " + maxHeight + ", " + quality + ", "
				+ compressFormat.toString() + ") IO Exception rotating scaled bitmap: " + ioe.getLocalizedMessage());
		}

		FileOutputStream out = null;
		File file = createCompressedImageFile(context, compressFormat);
		try {
			if (file != null) {
				out = new FileOutputStream(file.getAbsolutePath());
				// Write the compressed bitmap at the compressed file destination
				scaledBitmap.compress(compressFormat, quality, out);
			}
		} catch (FileNotFoundException e) {
			UtilLogger.e("ImageUtils", "compressImage(" + filePath + ", " + maxWidth + ", " + maxHeight + ", " + quality + ", "
				+ compressFormat.toString() + ") File Not Found Exception compressing the scaled bitmap");
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					UtilLogger.e("ImageUtils:", "compressImage(" + filePath + ", " + maxWidth + ", " + maxHeight + ", " + quality + ", "
						+ compressFormat.toString() + ") IO Exception closing File Output Stream! " + e.getLocalizedMessage());
				}
			}
		}

		return file;
	}

	/**
	 * Stores a provided file into the systems External Content Media Store allowing it to be accessible
	 * and indexed by Gallery apps such as Google Photos. This method additionally populates the
	 * media meta data with the provided title, display name, description, and more importantly
	 * date added and taken fields. Although images can be inserted to the media store using the
	 * {@link MediaStore.Images.Media#insertImage(ContentResolver, String, String, String)} method,
	 * this does not set these fields and so all images are stored at the chronological end of the Gallery.
	 *
	 * @param contentResolver	{@link ContentResolver} Used to insert the data into the Media Store
	 * @param image	{@link File} Data to store in the Media Store
	 * @param mimeType	{@link String} Mime type of the provided file e.g. "image/jpg"
	 * @param title	{@link String} Title of the Image, often the File name
	 * @param displayName	{@link String} Display Name of the Image
	 * @param description	{@link String} Description of the Image, or an empty Stri
	 * @return	{@link Uri} of the file within the Media Store if successfully inserted
	 */
	public static Uri storeImageInDeviceGallery(@NonNull ContentResolver contentResolver, @NonNull File image,
			@NonNull String mimeType, @NonNull String title, @NonNull String displayName, @Nullable String description) {

		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, title);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
		if (description == null) description = "";
		values.put(MediaStore.Images.Media.DESCRIPTION, description);
		values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
		values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(MediaStore.Images.Media.DATE_TAKEN, image.lastModified());
		values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());

		return contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
	}

	/**
	 * Returns an {@link ArrayList} of {@link RecentImage} objects which contain the users most recent
	 * local images stored in the {@link android.provider.MediaStore.Images} {@link android.content.ContentProvider}.
	 *
	 * The returned data contains information including the path and display name for these images,
	 * and are stored to show the most recent image first.
	 *
	 * This method requires the user to have granted the app the {@link permission#READ_EXTERNAL_STORAGE}
	 * permission which will require this method call to be surrounded by a method name that begins
	 * with "check" and ends with "permission".
	 *
	 * @param context	{@link Context}
	 * @param maxImages	Maximum number of recent images to retrieve
	 * @return	{@link ArrayList <RecentImage>} Array containing all the users recent image data
	 */
	@RequiresPermission(permission.READ_EXTERNAL_STORAGE)
	public static ArrayList<RecentImage> getRecentImages(@NonNull Context context, @IntRange(from=0) int maxImages) {
		ArrayList<RecentImage> recents = new ArrayList<>();
		Cursor cursor;

		// Retrieve the image uris in a cursor
		Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[]{
			Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.MIME_TYPE,
			MediaStore.Images.Media.SIZE,
			Media.DISPLAY_NAME
		};
		cursor = context.getContentResolver().query(uri, projection, null, null, null);

		if (cursor != null) {
			// Iterate backwards through the cursor to retrieve the most recent images
			int i = 0;
			for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
				recents.add(new RecentImage(cursor));
				i++;
				if (i >= maxImages) break;
			}

			cursor.close();
		}

		return recents;
	}

	/**
	 * Calculates the proper value for inSampleSize based on the actual and required dimensions of the Bitmap.
	 * If > 1 then the Bitmap decoder will downscale the original bitmap image, this value is the number
	 * of pixels in either dimension that correspond to a single pixel in the decoded Bitmap.
	 *
	 * @param options	{@link BitmapFactory.Options} of the original Bitmap
	 * @param reqWidth	Required width of the Bitmap
	 * @param reqHeight	Required height of the Bitmap
	 * @return	Bitmap inSampleSize used to downscale the original Bitmap
	 */
	private static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		final float totalPixels = width * height;
		final float totalReqPixelsCap = reqWidth * reqHeight * 2;

		while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
			inSampleSize++;
		}

		return inSampleSize;
	}

	/**
	 * Creates a {@link File} suitable for storing a compressed Image file, this convenience method
	 * will create a {@link File} in the default compressed files directory, using the current timestamp
	 * as a file name, appended with "_COMPRESSED", and an extension based off the provided
	 * {@link CompressFormat}.
	 *
	 * As the default directory for compressed files is within the app contexts' cache directory, the
	 * {@link permission#WRITE_EXTERNAL_STORAGE} is not required, but all files will be removed when
	 * {@link FileUtils#cleanUpCache(Context)} is called.
	 *
	 * @param context	{@link Context}
	 * @param compressFormat	{@link CompressFormat} defining the extension of the compressed image
	 * @return	{@link File} instance or null
	 */
	@Nullable
	private static File createCompressedImageFile(@NonNull Context context, @NonNull CompressFormat compressFormat) {
		String fileExtension = ".jpg";
		if (compressFormat.equals(CompressFormat.PNG)) {
			fileExtension = ".png";
		} else if (compressFormat.equals(CompressFormat.WEBP)) {
			fileExtension = ".webp";
		}

		String directoryPath = getCompressedDirectory(context);
		if (directoryPath != null) {
			final File directory = new File(directoryPath);
			if (!directory.exists()) {
				if (!directory.mkdirs()) {
					return null;
				} else {
					new File(directory, ".nomedia");
				}
			}

			String filename = FileUtils.getTimestampFileName() + "_COMPRESSED" + fileExtension;
			File file = new File(directory, filename);
			UtilLogger.d("ImageUtils", "createCompressedImageFile(" + compressFormat.toString()
				+ ") Successfully created file: " + file.getAbsolutePath());

			return file;
		}

		return null;
	}

	/**
	 * Returns the common directory for compressed images, this is used by the
	 * {@link ImageUtils#createCompressedImageFile(Context, CompressFormat)} method and stores the
	 * compressed images in an app local cache directory.
	 *
	 * @param context	{@link Context}
	 * @return	{@link String} File directory for compressed images
	 */
	@Nullable
	private static String getCompressedDirectory(@NonNull Context context) {
		File cacheDir = context.getCacheDir();
		if (cacheDir != null && cacheDir.isDirectory()) {
			return cacheDir.getAbsolutePath() + "/.compressed";
		}
		return null;
	}

	@Nullable
	public static Uri createImageUriFromExternalUri(Context context, Uri uri) {
		InputStream is = null;
		if (uri.getAuthority() != null) {
			try {
				is = context.getContentResolver().openInputStream(uri);
				Bitmap bmp = BitmapFactory.decodeStream(is);

				String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
					Locale.getDefault()).format(new Date());
				String filename = "IMG_" + timestamp + ".jpg";

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
				String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
					bmp, filename, null);
				return Uri.parse(path);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}finally {
				try {
					if (is != null) is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static byte[] getThumbnailFromVideoFile(@NonNull File videoFile) {
		Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
			videoFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		thumbnail.compress(CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	public static byte[] getThumbnailFromImageFile(@NonNull File imageFile,
			@IntRange(from=0) int width, @IntRange(from=0) int height) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inSampleSize = calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;

		Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(
			imageFile.getAbsolutePath(), options), width, height);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		thumbnail.compress(CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	/**
	 * Data class to wrap information about the users recent images, as retrieved from the
	 * {@link ImageUtils#getRecentImages(Context, int)} method.
	 *
	 * @author hedgehog lab
	 * @version 1.0
	 */
	public static class RecentImage implements Parcelable {
		private long mId;
		private String mPath;
		private long mDateModified = -1;
		private String mMimeType = null;
		private long mSize;
		private String mDisplayName;

		/**
		 * Creates a Recent Image object from the Media Store cursor, when available the class
		 * variables are retrieved from the cursor data and set on the object.
		 *
		 * @param cursor	{@link Cursor} from the {@link MediaStore}
		 */
		public RecentImage(@NonNull Cursor cursor) {
			int idIdx = cursor.getColumnIndex(Media._ID);
			int pathIdx = cursor.getColumnIndex(Media.DATA);
			int dateModifiedIdx = cursor.getColumnIndex(Media.DATE_TAKEN);
			int mimeTypeIdx = cursor.getColumnIndex(Media.MIME_TYPE);
			int sizeIdx = cursor.getColumnIndex(Media.SIZE);
			int displayNameIdx = cursor.getColumnIndex(Media.DISPLAY_NAME);

			if (idIdx != -1) {
				setId(cursor.getLong(idIdx));
			}
			if (pathIdx != -1) {
				setPath(cursor.getString(pathIdx));
			}
			if (dateModifiedIdx != -1) {
				setDateModified(cursor.getLong(dateModifiedIdx));
			}
			if (mimeTypeIdx != -1) {
				setMimeType(cursor.getString(mimeTypeIdx));
			}
			if (sizeIdx != -1) {
				setSize(cursor.getLong(sizeIdx));
			}
			if (displayNameIdx != -1) {
				setDisplayName(cursor.getString(displayNameIdx));
			}
		}

		/** Returns the ID of the Image **/
		public long getId() {
			return mId;
		}

		/** Sets the ID of the Image **/
		public void setId(long id) {
			mId = id;
		}

		/** Returns the File Path of the Image **/
		public String getPath() {
			return mPath;
		}

		/** Sets the File Path of the Image **/
		public void setPath(String path) {
			mPath = path;
		}

		/** Returns the Date Modified of the Image **/
		public long getDateModified() {
			return mDateModified;
		}

		/** Sets the Date modified of the Image **/
		public void setDateModified(long dateModified) {
			mDateModified = dateModified;
		}

		/** Returns the Mime Type of the Image **/
		public String getMimeType() {
			return mMimeType;
		}

		/** Sets the Mime Type of the Image **/
		public void setMimeType(String mimeType) {
			mMimeType = mimeType;
		}

		/** Returns the size in bytes of the Image **/
		public long getSize() {
			return mSize;
		}

		/** Sets the size in bytes of the Image **/
		public void setSize(long size) {
			mSize = size;
		}

		/** Returns the Display Name of the Image **/
		public String getDisplayName() {
			return mDisplayName;
		}

		/** Sets the Display Name of the Image **/
		public void setDisplayName(String displayName) {
			mDisplayName = displayName;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(this.mId);
			dest.writeString(this.mPath);
			dest.writeLong(this.mDateModified);
			dest.writeString(this.mMimeType);
			dest.writeLong(this.mSize);
			dest.writeString(this.mDisplayName);
		}

		protected RecentImage(Parcel in) {
			this.mId = in.readLong();
			this.mPath = in.readString();
			this.mDateModified = in.readLong();
			this.mMimeType = in.readString();
			this.mSize = in.readLong();
			this.mDisplayName = in.readString();
		}

		public static final Creator<RecentImage> CREATOR = new Creator<RecentImage>() {
			@Override
			public RecentImage createFromParcel(Parcel source) {
				return new RecentImage(source);
			}

			@Override
			public RecentImage[] newArray(int size) {
				return new RecentImage[size];
			}
		};

		@Override
		public String toString() {
			return "RecentImage{" +
				"mId=" + mId +
				", mPath='" + mPath + '\'' +
				", mDateModified=" + mDateModified +
				", mMimeType='" + mMimeType + '\'' +
				", mSize=" + mSize +
				", mDisplayName='" + mDisplayName + '\'' +
				'}';
		}
	}
}