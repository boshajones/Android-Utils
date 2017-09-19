package com.bosh.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import java.util.Locale;

/**
 * Creates an instance of the Application {@link Context} with a {@link Locale} specified by language,
 * this allows an application to change the language each activity implements based off user preference
 * as opposed to what the current system locale settings specify.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LanguageContextWrapper extends ContextWrapper {

	private LanguageContextWrapper(Context base) {
		super(base);
	}

	/**
	 * Call this method in the Activity attachBaseContext method providing the returned object to
	 * the super call
	 *
	 * @param context
	 * @param languageCode
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static ContextWrapper wrap(@NonNull Context context, @NonNull String languageCode) {
		Configuration config = context.getResources().getConfiguration();
		Locale sysLocale;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			sysLocale = getSystemLocale(config);
		} else {
			sysLocale = getSystemLocaleLegacy(config);
		}
		if (!languageCode.equals("") && !sysLocale.getLanguage().equals(languageCode)) {
			Locale locale = new Locale(languageCode);
			Locale.setDefault(locale);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				setSystemLocale(config, locale);
			} else {
				setSystemLocaleLegacy(config, locale);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				context = context.createConfigurationContext(config);
			} else {
				context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
			}
		}
		return new LanguageContextWrapper(context);
	}

	@SuppressWarnings("deprecation")
	public static Locale getSystemLocaleLegacy(Configuration config){
		return config.locale;
	}

	@TargetApi(Build.VERSION_CODES.N)
	public static Locale getSystemLocale(Configuration config){
		return config.getLocales().get(0);
	}

	@SuppressWarnings("deprecation")
	public static void setSystemLocaleLegacy(Configuration config, Locale locale){
		config.locale = locale;
	}

	@TargetApi(Build.VERSION_CODES.N)
	public static void setSystemLocale(Configuration config, Locale locale){
		config.setLocale(locale);
	}

}