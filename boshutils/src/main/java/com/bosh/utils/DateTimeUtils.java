package com.bosh.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Contains methods relating to date and time tasks, primarily parsing and displaying to the user.
 * This class currently focuses on the ISO 8601 date and time international standard.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DateTimeUtils {

	public static final String ISO8601_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static final String DISPLAY_TIMESTAMP_FORMAT = "HH:mm:ss yyyy-MM-dd";

	public static final String DISPLAY_TIMESTAMP_FORMAT_12_HRS = "K:mm a d MMMM yyyy";

	public static final String DISPLAY_TIMESTAMP_FORMAT_24_HRS = "HH:mm d MMMM yyyy";

	public static String getCurrentISO8601Timestamp() {
		return parseTimestampToISO8601(System.currentTimeMillis());
	}

	public static long parseISO8601ToMs(@Nullable String timestamp) {
		return parseTimestampToMs(ISO8601_TIMESTAMP_FORMAT, timestamp);
	}

	public static long parseTimestampToMs(@NonNull String format, @Nullable String timestamp) {
		if (timestamp == null) {
			return -1;
		}

		SimpleDateFormat f = new SimpleDateFormat(format, Locale.getDefault());
		try {
			Date d = f.parse(timestamp);
			return d.getTime();
		} catch (ParseException e) {
			UtilLogger.e("DateTimeUtils", "Parse Exception trying to parse timestamp to format: " + format, e);
		}
		return -1;
	}

	public static String parseTimestampToISO8601(long timestamp) {
		return parseTimestampToString(ISO8601_TIMESTAMP_FORMAT, timestamp);
	}

	public static String parseTimestampToString(@NonNull String format, long timestamp) {
		DateFormat df = new SimpleDateFormat(format, Locale.getDefault());
		return df.format(timestamp);
	}

	public static String get12Or24HourTimestamp(@NonNull Context context, long timestamp) {
		String format = DISPLAY_TIMESTAMP_FORMAT_12_HRS;
		if (android.text.format.DateFormat.is24HourFormat(context)) {
			format = DISPLAY_TIMESTAMP_FORMAT_24_HRS;
		}

		DateFormat df = new SimpleDateFormat(format, Locale.getDefault());
		return df.format(new Date(timestamp));
	}

	public static String getRelativeTimestamp(@NonNull Context context, long timestamp) {
		return getRelativeTimestamp(context, timestamp, DateUtils.MINUTE_IN_MILLIS);
	}

	public static String getRelativeTimestamp(@NonNull Context context, long timestamp, long relativeResolution) {
		long currentTime = System.currentTimeMillis();
		long oneMinuteAgo = currentTime - TimeUnit.MINUTES.toMillis(1);
		long oneWeekAgo = currentTime - TimeUnit.DAYS.toMillis(7);

		return getRelativeTimestamp(context, timestamp, relativeResolution, R.string.just_now, oneMinuteAgo, oneWeekAgo);
	}

	public static String getRelativeTimestamp(@NonNull Context context, long timestamp, long relativeResolution,
			@StringRes int lowerBoundText, long lowerBound, long upperBound) {

		long currentTime = System.currentTimeMillis();

		if (timestamp > currentTime || timestamp > lowerBound) {
			// In the future or less than the lower bound
			return context.getString(lowerBoundText);
		} else if (timestamp > upperBound) {
			// Between now and the upper bound
			return DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), relativeResolution).toString();
		} else {
			// Older than the upper bound
			SimpleDateFormat df = new SimpleDateFormat(DISPLAY_TIMESTAMP_FORMAT, Locale.getDefault());
			return df.format(timestamp);
		}
	}

}
