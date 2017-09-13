package com.bosh.boshutils;

import android.Manifest.permission;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.widget.Toast;
import java.io.File;

/**
 * Created by David Jones on 11/09/2017
 */
@SuppressWarnings({ "unused" })
public class IntentUtils {

	public static void launchApp(@NonNull Context context, @NonNull String packageName) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "launchApp(" + packageName + ")", anfe);
		}
	}

	public static boolean isAppInstalled(@NonNull Context context, @NonNull String packageName) {
		try {
			context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException ex) {
			return false;
		}
	}

	public static Intent getRestartIntent(Context context) {
		String packageName = context.getPackageName();
		Intent defaultIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		if (defaultIntent != null) {
			defaultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			return defaultIntent;
		}

		throw new IllegalStateException("Unable to determine default activity for " + packageName
			+ ". Does an activity specify the DEFAULT category in its intent filter?");
	}

	public static void launchAppByPackage(@NonNull Context context, @NonNull String packageName) {
		Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		if (launchIntent != null) {
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(launchIntent);
			} catch (ActivityNotFoundException anfe) {
				UtilLogger.e("IntentUtils", "Unable to launch application package");
			}
		}
	}

	public static void openAppSettings(@NonNull Context context) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", context.getPackageName(), null);
		intent.setData(uri);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openAppSettings() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_open_app_settings, Toast.LENGTH_SHORT).show();
		}
	}

	public static void openLocationSettings(@NonNull Context context) {
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		try {
			context.startActivity(settingsIntent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openLocationSettings() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_open_app_settings, Toast.LENGTH_SHORT).show();
		}
	}

	public static void openPhoneDialler(@NonNull Context context, @NonNull String phoneNumber) {
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openPhoneDialler() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_open_dialler, Toast.LENGTH_SHORT).show();
		}
	}

	@RequiresPermission(permission.CALL_PHONE)
	public static void startPhoneCall(@NonNull Context context, @NonNull String phoneNumber) {
		Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "startPhoneCall() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_make_phone_calls, Toast.LENGTH_SHORT).show();
		}
	}

	public static void openSmsApp(@NonNull Context context, @NonNull String phoneNumber, @NonNull String content) {
		Uri uri = Uri.parse("smsto:" + phoneNumber);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", content);
		intent.putExtra(Intent.EXTRA_TEXT, content);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openSmsApp() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_open_sms, Toast.LENGTH_SHORT).show();
		}
	}

	@RequiresPermission(permission.SEND_SMS)
	public static boolean sendSMS(@NonNull String phoneNumber, @NonNull String content,
			@Nullable PendingIntent sentIntent, @Nullable PendingIntent deliveryIntent) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNumber, null, content, null, null);
			return true;
		} catch (Exception e) {
			UtilLogger.e("IntentUtils", "sendSMS() Exception", e);
		}
		return false;
	}

	public static void openEmailApp(@NonNull Context context, @NonNull String emailAddress,
			@Nullable String subject, @Nullable String body) {

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("plain/text");

		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });

		if (!StringUtils.isEmpty(subject)) {
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}

		if (!StringUtils.isEmpty(body)) {
			intent.putExtra(Intent.EXTRA_TEXT, body);
		}

		context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));
	}

	public static void shareText(@NonNull Context context, @NonNull String content) {
		Intent intent = getShareTextIntent(content);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "shareText() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_share_string, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getShareTextIntent(@NonNull String content) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, content);
		return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	public static void shareImage(@NonNull Context context, @Nullable String content, @NonNull File image) {
		Intent intent = getShareImageIntent(content, image);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "shareImage() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_share_image, Toast.LENGTH_SHORT).show();
		}
	}

	@Nullable
	public static Intent getShareImageIntent(@Nullable String content, @NonNull File image) {
		if (!image.exists()) return null;
		return getShareImageIntent(content, Uri.fromFile(image));
	}

	public static void shareImage(@NonNull Context context, @Nullable String content, @NonNull Uri uri) {
		Intent intent = getShareImageIntent(content, uri);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "shareImage() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_share_image, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getShareImageIntent(@Nullable String content, @NonNull Uri uri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, content);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType("image/*");
		return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	public static void openCamera(@NonNull Activity activity, @NonNull Uri photoOutput,
			int requestCode, @Nullable Bundle options, boolean openFrontFacingCamera) {

		Intent intent = getCameraIntent(photoOutput, openFrontFacingCamera);
		try {
			activity.startActivityForResult(intent, requestCode, options);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openCamera() Activity Not Found", anfe);
			Toast.makeText(activity, R.string.error_cannot_open_camera, Toast.LENGTH_SHORT).show();
		}
	}

	public static void openCamera(@NonNull Fragment fragment, @NonNull Uri photoOutput,
			int requestCode, @Nullable Bundle options, boolean openFrontFacingCamera) {

		Intent intent = getCameraIntent(photoOutput, openFrontFacingCamera);
		try {
			fragment.startActivityForResult(intent, requestCode, options);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openCamera() Activity Not Found", anfe);
			Toast.makeText(fragment.getContext(), R.string.error_cannot_open_camera, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getCameraIntent(@NonNull Uri photoOutput, boolean openFrontFacingCamera) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoOutput);

		if (openFrontFacingCamera) {
			intent.putExtra("android.intent.extras.CAMERA_FACING",
				android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
			intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
			intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
		}

		return intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	@RequiresPermission(permission.READ_EXTERNAL_STORAGE)
	public static void openPictureGallery(@NonNull Activity activity, boolean allowMultiple, int requestCode, @Nullable Bundle options) {
		Intent intent = getPictureGalleryIntent(allowMultiple);
		try {
			activity.startActivityForResult(intent, requestCode, options);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openPictureGallery() Activity Not Found", anfe);
			Toast.makeText(activity, R.string.error_cannot_open_gallery, Toast.LENGTH_SHORT).show();
		}
	}

	@RequiresPermission(permission.READ_EXTERNAL_STORAGE)
	public static void openPictureGallery(@NonNull Fragment fragment, boolean allowMultiple, int requestCode, @Nullable Bundle options) {
		Intent intent = getPictureGalleryIntent(allowMultiple);
		try {
			fragment.startActivityForResult(intent, requestCode, options);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openPictureGallery() Activity Not Found", anfe);
			Toast.makeText(fragment.getContext(), R.string.error_cannot_open_gallery, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getPictureGalleryIntent(boolean allowMultiple) {
		Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
		}
		return intent;
	}

	@RequiresPermission(permission.READ_EXTERNAL_STORAGE)
	public static void openVideoGallery(@NonNull Activity activity, int requestCode, @Nullable Bundle options) {
		Intent intent = getVideoGalleryIntent();
		try {
			activity.startActivityForResult(intent, requestCode, options);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openVideoGallery() Activity Not Found", anfe);
			Toast.makeText(activity, R.string.error_cannot_open_gallery, Toast.LENGTH_SHORT).show();
		}
	}

	@RequiresPermission(permission.READ_EXTERNAL_STORAGE)
	public static void openVideoGallery(@NonNull Fragment fragment, int requestCode, @Nullable Bundle options) {
		Intent intent = getVideoGalleryIntent();
		try {
			fragment.startActivityForResult(intent, requestCode, options);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openVideoGallery() Activity Not Found", anfe);
			Toast.makeText(fragment.getContext(), R.string.error_cannot_open_gallery, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getVideoGalleryIntent() {
		Intent intent = new Intent();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			intent.setTypeAndNormalize("video/*");
		} else {
			intent.setType("video/*");
		}
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		return intent;
	}

	public static void openMapsAtCoordinates(@NonNull Context context, double latitude,
			double longitude, @Nullable String markerTitle) {

		Intent intent = getMapsCoordsIntent(latitude, longitude, markerTitle);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openMapsAtCoordinates() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_open_maps, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getMapsCoordsIntent(double latitude, double longitude, @Nullable String markerTitle) {
		StringBuilder uri = new StringBuilder();
		uri.append("geo:");
		uri.append(latitude);
		uri.append(",");
		uri.append(longitude);
		uri.append("?q=");
		uri.append(latitude);
		uri.append(",");
		uri.append(longitude);

		if (!StringUtils.isEmpty(markerTitle)) {
			uri.append("(");
			uri.append(markerTitle);
			uri.append(")");
		}

		return new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
	}

	public static void openGoogleMapsNavigation(@NonNull Context context, double latFrom, double longFrom,
			double latTo, double longTo) {

		Intent intent = getGoogleMapsNavigationIntent(latFrom, longFrom, latTo, longTo);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			UtilLogger.e("IntentUtils", "openGoogleMapsNavigation() Activity Not Found", anfe);
			Toast.makeText(context, R.string.error_cannot_open_maps, Toast.LENGTH_SHORT).show();
		}
	}

	@NonNull
	public static Intent getGoogleMapsNavigationIntent(double latFrom, double longFrom, double latTo, double longTo) {
		StringBuilder uri = new StringBuilder();
		uri.append("http://maps.google.com/maps?saddr=");
		uri.append(latFrom);
		uri.append(",");
		uri.append(longFrom);
		uri.append("&daddr=");
		uri.append(latTo);
		uri.append(",");
		uri.append(longTo);

		return new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
	}
}
