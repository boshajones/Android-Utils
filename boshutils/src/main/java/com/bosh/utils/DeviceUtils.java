package com.bosh.utils;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;

/**
 * Handles retrieving information about the users current device and for the current application.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DeviceUtils {

	@Nullable
	@SuppressLint("HardwareIds")
	@RequiresPermission(permission.READ_PHONE_STATE)
	public static String getIMEI(@NonNull Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			return telephonyManager.getDeviceId();
		}
		return null;
	}

	@Nullable
	@SuppressLint("HardwareIds")
	@RequiresPermission(permission.READ_PHONE_STATE)
	public static String getSimOperatorName(@NonNull Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			return telephonyManager.getSimOperatorName();
		}
		return null;
	}

	@Nullable
	@SuppressLint("HardwareIds")
	@RequiresPermission(permission.READ_PHONE_STATE)
	public static String getSimSerial(@NonNull Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			return telephonyManager.getSimSerialNumber();
		}
		return null;
	}

	@Nullable
	@SuppressLint("HardwareIds")
	public static String getDeviceID(@NonNull Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	@SuppressLint("HardwareIds")
	@RequiresPermission(permission.READ_PHONE_STATE)
	public static String getBuildSerial() {
		if (VERSION.SDK_INT >= VERSION_CODES.O) {
			return Build.getSerial();
		} else {
			return Build.SERIAL;
		}
	}

	public static String getManufacturerModel() {
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER)) {
			model = Build.MANUFACTURER + " " + model;
		}
		return model;
	}

	@Nullable
	public static String getAppVersionName(@NonNull Context context) {
		PackageInfo info = getPackageInfo(context);
		if (info != null) {
			return info.versionName;
		}
		return null;
	}


	public static int getAppVersionCode(@NonNull Context context) {
		PackageInfo info = getPackageInfo(context);
		if (info != null) {
			return info.versionCode;
		}
		return -1;
	}

	@Nullable
	private static PackageInfo getPackageInfo(@NonNull Context context) {
		PackageManager manager = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e2) {
			// Version info not included if exception thrown
		}
		return info;
	}

	/**
	 * Returns a string of device and app info as follows
	 * Android version: {sdk version code}
	 * Device: {hardware model}
	 * App Version: {version name (version number)}
	 *
	 * @param context	{@link Context}
	 * @return	String containing app and device info
	 */
	public static String getDeviceAppInfo(Context context) {
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append("Android version: ");
		ssb.append(String.valueOf(VERSION.SDK_INT));
		ssb.append("\n");
		ssb.append("Device :");
		ssb.append(getManufacturerModel());

		ssb.append("\n");
		ssb.append("App version: ");
		ssb.append(getAppVersionName(context));
		ssb.append(" (");
		ssb.append(String.valueOf(getAppVersionCode(context)));
		ssb.append(")");

		ssb.append("\n");

		return ssb.toString();
	}

}
