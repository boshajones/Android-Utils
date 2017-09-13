package com.bosh.boshutils;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by David Jones on 05/09/2017
 */
@SuppressWarnings({ "unused" })
public class UtilLogger {

	private static boolean DEBUG_LOGS_ENABLED = false;
	private static boolean ERROR_LOGS_ENABLED = true;

	public static boolean isDebugLogsEnabled() {
		return DEBUG_LOGS_ENABLED;
	}

	public static void setDebugLogsEnabled(boolean debugLogsEnabled) {
		DEBUG_LOGS_ENABLED = debugLogsEnabled;
	}

	public static boolean isErrorLogsEnabled() {
		return ERROR_LOGS_ENABLED;
	}

	public static void setErrorLogsEnabled(boolean errorLogsEnabled) {
		ERROR_LOGS_ENABLED = errorLogsEnabled;
	}

	public static void chunkLog(@NonNull String tag, @NonNull String string) {
		if (string.length() > 4000) {
			int chunkCount = string.length() / 4000;
			Log.i( "UtilLogger", "Debug long chunk length = " + string.length() + " chunks = " + chunkCount);
			for (int i = 0; i <= chunkCount; i++) {
				int max = 4000 * (i + 1);
				if (max >= string.length()) {
					d(tag, string.substring(4000 * i));
				} else {
					d(tag, string.substring(4000 * i, max));
				}
			}
		} else {
			d(tag, string);
		}
	}

	static void d(@NonNull String tag, @NonNull String msg) {
		if (DEBUG_LOGS_ENABLED) {
			Log.d(tag, msg);
		}
	}

	static void e(@NonNull String tag, @NonNull String msg) {
		if (ERROR_LOGS_ENABLED) {
			Log.e(tag, msg);
		}
	}

	static void e(@NonNull String tag, @NonNull String msg, @NonNull Throwable throwable) {
		if (ERROR_LOGS_ENABLED) {
			Log.e(tag, msg, throwable);
		}
	}
}
