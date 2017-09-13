package com.bosh.utils;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by David Jones on 11/09/2017
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkUtils {

	public static void openWirelessSettings(@NonNull Context context) {
		Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Nullable
	@RequiresPermission(permission.ACCESS_NETWORK_STATE)
	private static NetworkInfo getActiveNetworkInfo(@NonNull Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			return connectivityManager.getActiveNetworkInfo();
		}
		return null;
	}

	@RequiresPermission(permission.ACCESS_NETWORK_STATE)
	public static boolean isConnected(@NonNull Context context) {
		NetworkInfo info = getActiveNetworkInfo(context);
		return info != null && info.isConnected();
	}

	@RequiresPermission(permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectedOrConnecting(@NonNull Context context) {
		NetworkInfo info = getActiveNetworkInfo(context);
		return info != null && info.isConnectedOrConnecting();
	}

	@RequiresPermission(permission.ACCESS_NETWORK_STATE)
	public static boolean isWifiConnected(@NonNull Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
			.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null
			&& cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
	}

	@Nullable
	public static String getNetworkOperatorName(@NonNull Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm != null ? tm.getNetworkOperatorName() : null;
	}

	@SuppressLint("WifiManagerLeak")
	public static boolean isWifiEnabled(@NonNull Context context) {
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		return wifiManager != null && wifiManager.isWifiEnabled();
	}

	@SuppressLint("PrivateApi")
	public static boolean isDataEnabled(@NonNull Context context) {
		try {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (tm != null) {
				Method getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled");
				if (getMobileDataEnabledMethod != null) {
					return (boolean) getMobileDataEnabledMethod.invoke(tm);
				}
			}
		} catch (Exception e) {
			UtilLogger.e("NetworkUtils", "isDataEnabled() Unable to invoke method", e);
		}
		return false;
	}

	private static final HashMap<ConnectivityListener, ConnectivityReceiver> sConnectivityListenerMap = new HashMap<>();

	@RequiresPermission(permission.ACCESS_NETWORK_STATE)
	public static void registerConnectivityReceiver(@NonNull Context context, @NonNull ConnectivityListener listener, boolean holdWeakReference) {
		if (sConnectivityListenerMap.containsKey(listener)) {
			return;
		}

		final ConnectivityReceiver receiver = new ConnectivityReceiver(listener, holdWeakReference);
		sConnectivityListenerMap.put(listener, receiver);
		context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		receiver.notify(isConnected(context));
	}

	@RequiresPermission(permission.ACCESS_NETWORK_STATE)
	public static void unregisterConnectivityReceiver(@NonNull Context context, @Nullable ConnectivityListener listener) {
		final ConnectivityReceiver receiver = sConnectivityListenerMap.remove(listener);
		if (receiver != null) {
			context.unregisterReceiver(receiver);
		}
	}

	public interface ConnectivityListener {
		void onConnectivityChange(boolean isConnected);
	}

	private static class ConnectivityReceiver extends BroadcastReceiver {

		private ConnectivityListener mConnectivityListenerStrongReference;
		private WeakReference<ConnectivityListener> mConnectivityListenerWeakReference;
		private Boolean mLastConnectedStatus;

		ConnectivityReceiver(@NonNull ConnectivityListener listener, boolean useWeakReference) {
			if (useWeakReference) {
				mConnectivityListenerWeakReference = new WeakReference<>(listener);
			} else {
				mConnectivityListenerStrongReference = listener;
			}
		}

		@Override
		@RequiresPermission(permission.ACCESS_NETWORK_STATE)
		public void onReceive(@NonNull Context context, @NonNull Intent intent) {
			notify(isConnected(context));
			unregisterConnectivityReceiver(context, null);
		}

		private void notify(boolean isConnected) {
			if (mLastConnectedStatus == null || mLastConnectedStatus != isConnected) {
				mLastConnectedStatus = isConnected;

				ConnectivityListener listener = getConnectivityListener();
				if (listener != null) {
					listener.onConnectivityChange(mLastConnectedStatus);
				}
			}
		}

		@Nullable
		private ConnectivityListener getConnectivityListener() {
			if (mConnectivityListenerStrongReference != null) {
				return mConnectivityListenerStrongReference;
			} else {
				ConnectivityListener listener = null;
				if (mConnectivityListenerWeakReference != null) {
					listener = mConnectivityListenerWeakReference.get();
				}
				if (listener == null) {
					UtilLogger.e("NetworkUtils", "Connectivity Listener Weak Reference Null");
				}
				return listener;
			}
		}
	}

}
