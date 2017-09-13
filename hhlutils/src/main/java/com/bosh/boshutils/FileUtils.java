package com.bosh.boshutils;

import android.Manifest.permission;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilities class containing several common file utility methods useful across various projects
 *
 * @author hedgehog lab
 * @version 1.0
 */
@SuppressWarnings({ "unused" })
public class FileUtils {

	/**
	 * Defines the various locations supported by the File Provider setup on this app.
	 * To add another storage location, update the xml/file_provider.xml file and add another value here.
	 *
	 * *Note* Although DO_NOT_STORE will set the file to delete on exit, the Android lifecycle doesn't
	 * include VM termination so there is no guarantee the file will be removed. It's suggested that
	 * you call {@link #cleanUpCache(Context)} once you are finished with the file.
	 */
	@IntDef({FileStorageLocations.DO_NOT_STORE, FileStorageLocations.INTERNAL_CACHE, FileStorageLocations.DCIM_CAMERA})
	@Retention(RetentionPolicy.SOURCE)
	public @interface FileStorageLocations {
		int DO_NOT_STORE = 0;
		int INTERNAL_CACHE = 1;
		int DCIM_CAMERA = 2;
	}

	/**
	 * Creates a {@link Uri} reference for the provided {@link File}
	 *
	 * @param context	{@link Context}
	 * @param file	{@link File}
	 * @return	{@link Uri}
	 */
	public static Uri getFileUri(@NonNull Context context, @NonNull File file) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return FileProvider.getUriForFile(context,
				context.getApplicationContext().getPackageName() + ".provider", file);
		} else {
			return Uri.fromFile(file);
		}
	}

	/**
	 * Removes the temporary and compressed file directories, removing any files stored within. This
	 * method should be called if you created files with the {@link FileStorageLocations#DO_NOT_STORE}
	 * value, or created compressed images with {@link ImageUtils#createCompressedImageFile(Context, CompressFormat)}
	 * method.
	 *
	 * @param context	{@link Context}
	 */
	public static void cleanUpCache(@NonNull Context context) {
		File cacheDir = context.getExternalCacheDir();
		if (cacheDir != null && cacheDir.isDirectory()) {
			File cleanUpTemp = new File(cacheDir, "/.temporary");
			deleteDir(cleanUpTemp);

			File cleanUpCompressed = new File(cacheDir, "/.compressed");
			deleteDir(cleanUpCompressed);
		}
	}

	/**
	 * Creates a new {@link File} instance from a file name and extension, along with one of the
	 * {@link FileStorageLocations} values to represent a directory. This method will return null if
	 * the directory is unable to be located or created.
	 *
	 * This method requires the user to have granted the app the {@link permission#WRITE_EXTERNAL_STORAGE}
	 * permission which will require this method call to be surrounded by a method name that begins
	 * with "check" and ends with "permission".
	 *
	 * @param context	{@link Context}
	 * @param fileName	Name of the {@link File} being created
	 * @param extension	Extension of the {@link File} being created, e.g. ".jpg"
	 * @param fileStorageLocation	{@link FileStorageLocations} Directory to create the File
	 * @return	{@link File} instance or null
	 */
	@Nullable
	@RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
	public static File createFile(@NonNull Context context, @NonNull String fileName,
			@NonNull String extension, @FileStorageLocations int fileStorageLocation) {
		String directoryPath = getDirectory(context, fileStorageLocation);
		File file = createFile(context, directoryPath, fileName, extension);
		if (file != null && fileStorageLocation == FileStorageLocations.DO_NOT_STORE) {
			file.deleteOnExit();
		}

		return file;
	}

	@Nullable
	@RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
	public static File createFile(@NonNull Context context, @Nullable String directoryPath,
		@NonNull String fileName, @NonNull String extension) {

		if (directoryPath != null) {
			final File directory = new File(directoryPath);
			if (!directory.exists()) {
				if (!directory.mkdirs()) {
					UtilLogger.e("FileUtils", "createFile(" + fileName + ", " + extension + ", "
						+ directoryPath + ")" + " Unable to create directory or all required parent directories");
					return null;
				}
			}

			if (!extension.startsWith(".")) {
				extension = "." + extension;
			}

			String filename = fileName + extension;
			File file = new File(directory, filename);
			UtilLogger.d("FileUtils", "createFile(" + fileName + ", " + extension + ", "
				+ directoryPath + ") " + " Successfully created file: " + file.getAbsolutePath());
			return file;
		}

		UtilLogger.e("FileUtils", "createFile(" + fileName + ", " + extension + ", "
			+ directoryPath + ")" + " Unable to create file as directory path was null");
		return null;
	}

	/**
	 * Creates a {@link File} suitable for an Image, this convenience method will create a {@link File}
	 * in the directory specified by the {@link FileStorageLocations} using the current timestamp
	 * as a file name, prepended with "IMG_", and ".jpg" as the extension.
	 *
	 * This method requires the user to have granted the app the {@link permission#WRITE_EXTERNAL_STORAGE}
	 * permission which will require this method call to be surrounded by a method name that begins
	 * with "check" and ends with "permission".
	 *
	 * @param context	{@link Context}
	 * @param fileStorageLocation	{@link FileStorageLocations} Directory to create the File
	 * @return	{@link File} instance or null
	 */
	@Nullable
	@SuppressWarnings("MissingPermission")
	@RequiresPermission(permission.WRITE_EXTERNAL_STORAGE)
	public static File createImageFile(@NonNull Context context, @FileStorageLocations int fileStorageLocation) {
		return createFile(context, "IMG_" + getTimestampFileName(), ".jpg", fileStorageLocation);
	}

	/**
	 * Attempts to retrieve the display name of a file for a given {@link Uri}. All files retrievable
	 * via the Intent#CATEGORY_OPENABLE call should contain a display name, if the Uri does not support
	 * display name or the Uri is not accessible then this method will return the trailing string
	 * from the Uri path.
	 *
	 * @param context	{@link Context}
	 * @param uri	{@link Uri} of the file to retrieve the display name for
	 * @return	{@link String} Display name of the file, which could be the end of the Uri path or null
	 */
	@Nullable
	public static String getFileDisplayName(@NonNull Context context, @NonNull Uri uri) {
		String result = null;
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				if (idx != -1) {
					result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		if (result == null) {
			result = uri.getPath();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}

	/**
	 * Returns the path of a {@link File} represented by a {@link Uri}. This will retrieve the
	 * path for Storage Access Framework Documents, as well as retrieving the "_data" field from
	 * the MediaStore, and handling other file based {@link android.content.ContentProvider}s.
	 *
	 * This method will return null if the Uri does not reference a real file, or the file cannot
	 * be retrieved from the devices {@link android.content.ContentProvider}s.
	 *
	 * @param context {@link Context}
	 * @param uri {@link Uri} of the file to find
	 * @return {@link String} Path of the real file
	 */
	public static String getFilePath(@NonNull Context context, @NonNull Uri uri) {
		UtilLogger.d("FileUtils","getFilePath() - Authority: " + uri.getAuthority() +
			", Fragment: " + uri.getFragment() + ", Port: " + uri.getPort() +
			", Query: " + uri.getQuery() + ", Scheme: " + uri.getScheme() +
			", Host: " + uri.getHost() + ", Segments: " + uri.getPathSegments().toString());

		// DocumentProvider
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract
			.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(uri)) {
				// DownloadsProvider
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris
					.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(uri)) {
				// MediaProvider
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {split[1]};
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// MediaStore (and general)
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}
			return getDataColumn(context, uri, null, null);
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			// File
			return uri.getPath();
		}
		return null;
	}

	/**
	 * Returns the size of a file, in a human readable format e.g. 10.3 MB
	 *
	 * @param file {@link File} to retrieve the size
	 * @return	{@link String} Human readable size format
	 */
	public static String getReadableFileSize(@NonNull File file) {
		final int BYTES_IN_KILOBYTES = 1024;
		final DecimalFormat dec = new DecimalFormat("###.#");
		final String KILOBYTES = " KB";
		final String MEGABYTES = " MB";
		final String GIGABYTES = " GB";
		float fileSize = 0;
		String suffix = KILOBYTES;

		long size = file.length();
		if (size > BYTES_IN_KILOBYTES) {
			fileSize = size / BYTES_IN_KILOBYTES;
			if (fileSize > BYTES_IN_KILOBYTES) {
				fileSize = fileSize / BYTES_IN_KILOBYTES;
				if (fileSize > BYTES_IN_KILOBYTES) {
					fileSize = fileSize / BYTES_IN_KILOBYTES;
					suffix = GIGABYTES;
				} else {
					suffix = MEGABYTES;
				}
			}
		}
		return String.valueOf(dec.format(fileSize) + suffix);
	}

	/**
	 * Returns a {@link String} of the current timestamp with millisecond precision, in the yyyyMMdd_HHmmss.SSS format
	 */
	public static String getTimestampFileName() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss.SSS",
			Locale.getDefault()).format(new Date());
	}

	/**
	 * Deletes a {@link File} instance, if the {@link File} was a directory, all children of the
	 * directory will also be removed in a recursive manner.
	 *
	 * @param dir	{@link File} to be deleted
	 * @return	True if the {@link File} and any children were deleted, else false
	 */
	private static boolean deleteDir(@Nullable File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (String aChildren : children) {
				boolean success = deleteDir(new File(dir, aChildren));
				if (!success) {
					return false;
				}
			}
		}

		boolean deleted = dir != null && dir.delete();
		if (dir != null) {
			UtilLogger.d("FileUtils","deleteDir( " + dir.getAbsolutePath() + ") Successful : " + deleted);
		}

		return deleted;
	}

	/**
	 * Returns the file directory most appropriate for the provided {@link FileStorageLocations} IntDef.
	 * Whenever the {@link FileStorageLocations} values are updated, this method should also be
	 * updated in order to specify how to retrieve the directory specified.
	 *
	 * {@link FileStorageLocations#DO_NOT_STORE} returns a directory local to the applications cache,
	 * inside a folder called "/.temporary". This directory is hidden from the user and will be cleaned
	 * up with any call to {@link FileUtils#cleanUpCache(Context)}
	 *
	 * {@link FileStorageLocations#INTERNAL_CACHE} returns a directory local to the applications cache,
	 * however unlike DO_NOT_STORE, this directory is not hidden from the user, nor will it be cleaned
	 * up. This is also the directory used if the users external media isn't mounted or accessible.
	 *
	 * {@link FileStorageLocations#DCIM_CAMERA} returns the devices Camera directory where all
	 * images taken using the devices Camera are stored. Storing an image here removes it from control
	 * of the applications cache and so will persist across clearing app data and re-installations.
	 *
	 * @param context	{@link Context}
	 * @param location	{@link FileStorageLocations} Determining the directory to retrieve
	 * @return	{@link String} Representing the file directory, or null if inaccessible
	 */
	@Nullable
	static String getDirectory(@NonNull Context context, @FileStorageLocations int location) {
		if (location == FileStorageLocations.DO_NOT_STORE) {
			File cacheDir = context.getExternalCacheDir();
			if (cacheDir != null && cacheDir.isDirectory()) {
				return cacheDir.getAbsolutePath() + "/.temporary";
			}
		}

		boolean externalMediaMounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		if (!externalMediaMounted || location == FileStorageLocations.INTERNAL_CACHE) {
			File cacheDir = context.getExternalCacheDir();
			if (cacheDir != null && cacheDir.isDirectory()) {
				return cacheDir.getAbsolutePath();
			}
		}

		if (!externalMediaMounted) {
			UtilLogger.e("FileUtils","getDirectory( " + location + ") Cache "
				+ "inaccessible and external media storage not mounted, returning null directory");
			return null;
		}

		if (location == FileStorageLocations.DCIM_CAMERA) {
			File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			if (dcim != null) {
				return dcim.getAbsolutePath() + "/Camera";
			}
		}

		return null;
	}

	/**
	 * Returns the data stored in the "_data" field of the {@link Cursor} accessed by querying the
	 * {@link android.content.ContentResolver} for the provided {@link Uri}. If the Cursor does
	 * contain this column, for system storage frameworks such as the {@link MediaStore} this is the
	 * absolute file path of the file determined by the {@link Uri}. If this column doesn't exist
	 * then null is returned.
	 *
	 * @param context	{@link Context}
	 * @param uri The URI, using the content:// scheme, for the content to retrieve.
	 * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
	 * 			(excluding the WHERE itself). Passing null will return all rows for the given URI.
	 * @param selectionArgs You may include ?s in selection, which will be replaced by the values from
	 * 			selectionArgs, in the order that they appear in the selection. The values will be bound as Strings.
	 * @return	Data stored in the "_data" column, else null
	 */
	private static String getDataColumn(@NonNull Context context, @NonNull Uri uri,
			@Nullable String selection, @Nullable String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/** Returns if the {@link Uri} provided is from the External Storage Provider **/
	private static boolean isExternalStorageDocument(@NonNull Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/** Returns if the {@link Uri} provided is from the Downloads Provider **/
	private static boolean isDownloadsDocument(@NonNull Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/** Returns if the {@link Uri} provided is from the Media Provider **/
	private static boolean isMediaDocument(@NonNull Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/** Returns if the {@link Uri} provided is from Google Photos specifically **/
	private static boolean isGooglePhotosUri(@NonNull Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	/**
	 * Reads a {@link File} instance into a byte array
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFile(final File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int bytesRead;
		while ((bytesRead = fis.read(b)) != -1) {
			bos.write(b, 0, bytesRead);
		}
		return bos.toByteArray();
	}

	/**
	 * Writes a byte array to a {@link File} stored at the provided file path
	 *
	 * @param bytes
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static File writeFile(final byte[] bytes, final String filePath) throws IOException {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(filePath);
			fos.write(bytes);
			return new File(filePath);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
					UtilLogger.e("FileUtils","writeFile() cannot close File Output Stream: " + ioe.getLocalizedMessage());
				}
			}
		}
	}

	public static void writeToFile(File toWrite, String data) {
		OutputStreamWriter outputStreamWriter = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(toWrite);
			outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			outputStreamWriter.write(data);
		} catch (IOException e) {
			UtilLogger.e("FileUtils","File write failed: " + e.toString());
		} finally {
			if (outputStreamWriter != null) {
				try {
					outputStreamWriter.close();
				} catch (IOException ioe) {
					UtilLogger.e("FileUtils","Unable to close output stream writer: " + ioe.getLocalizedMessage());
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException ioe) {
					UtilLogger.e("FileUtils","Unable to close file output stream: " + ioe.getLocalizedMessage());
				}
			}
		}
	}

	public static void writeBytesToFile(InputStream is, File file) throws IOException {
		FileOutputStream fos = null;
		try {
			byte[] data = new byte[2048];
			int nbread = 0;
			fos = new FileOutputStream(file);
			while((nbread=is.read(data))>-1){
				fos.write(data,0,nbread);
			}
		}
		catch (Exception ex) {
			UtilLogger.e("FileUtils", ex.getLocalizedMessage(), ex);
		}
		finally{
			if (fos!=null){
				fos.close();
			}
		}
	}


}
