package com.bosh.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnimRes;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Handles common tasks used throughout multiple applications that don't necessarily fit within
 * any of the other Utility classes.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CommonUtils {

	/**
	 * Launches a Custom Tab opening on the provided URL string. If Custom Tabs are not available then
	 * the system falls back to launching an external browser, otherwise the user is kept within the
	 * application. This method provides default colours and animations to the opening of custom tabs,
	 * these can be modified or the {@link #openLink(Context, String, int, int, int, int, int, int)}
	 * method can be used to specify colours and animations.
	 *
	 * @param context	{@link Context}
	 * @param urlString	{@link String} value of the URL to launch, http is automatically appended if required
	 */
	public static void openLink(@NonNull Context context, @Nullable String urlString) {
		@ColorRes int toolbarColor = android.R.color.black;
		@ColorRes int secondaryToolbarColor = android.R.color.white;
		@AnimRes int startEnterRes = R.anim.slide_in_from_bottom;
		@AnimRes int startExitRes = R.anim.fade_out;
		@AnimRes int exitEnterRes = R.anim.fade_in;
		@AnimRes int exitExitRes = R.anim.slide_out_to_bottom;

		openLink(context, urlString, toolbarColor, secondaryToolbarColor, startEnterRes,
			startExitRes, exitEnterRes, exitExitRes);
	}

	/**
	 * Launches a Custom Tab opening on the provided URL string. If Custom Tabs are not available then
	 * the system falls back to launching an external browser, otherwise the user is kept within the
	 * application. This methods allows specifiying the colours and animations used when opening
	 * the Custom Tab.
	 *
	 * @param context	{@link Context}
	 * @param urlString	{@link String} value of the URL to launch, http is automatically appended if required
	 * @param toolbarColor	Colour Resource value for the Toolbar
	 * @param secondaryToolbarColor	Color Resource value for the Toolbar views (title etc)
	 * @param startEnterAnimRes	Animation Resource value for the tab entrance
	 * @param startExitAnimRes	Animation Resource value for the application exit
	 * @param exitEnterAnimRes	Animation Resource value for the application entrance
	 * @param exitExitAnimRes	Animation Resource value for the tab exit
	 */
	public static void openLink(@NonNull Context context, @Nullable String urlString, @ColorRes int toolbarColor,
		@ColorRes int secondaryToolbarColor, @AnimRes int startEnterAnimRes, @AnimRes int startExitAnimRes,
		@AnimRes int exitEnterAnimRes, @AnimRes int exitExitAnimRes) {
		if (urlString == null) return;

		if (!(urlString.startsWith("http") || (urlString.startsWith("https")))) {
			urlString = "http://" + urlString;
		}

		Uri uri = Uri.parse(urlString);

		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		builder.setToolbarColor(ContextCompat.getColor(context, toolbarColor));
		builder.setSecondaryToolbarColor(ContextCompat.getColor(context, secondaryToolbarColor));
		builder.setStartAnimations(context, startEnterAnimRes, startExitAnimRes);
		builder.setExitAnimations(context, exitEnterAnimRes, exitExitAnimRes);

		CustomTabsIntent customTabsIntent = builder.build();
		customTabsIntent.launchUrl(context, uri);
	}

	/**
	 * Determines if a colour is regarded as 'dark' this is based off the definition for luminance of digital formats
	 * https://en.wikipedia.org/wiki/Luma_%28video%29
	 *
	 * @param color	Resolved colour integer to determine luminance of
	 * @return	True if the colour can be considered 'dark'
	 */
	public static boolean isColorDark(@ColorInt int color) {
		double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) +
			0.114 * Color.blue(color)) / 255;

		return darkness >= 0.5;
	}

	public static boolean isMiUi() {
		return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
	}

	private static String getSystemProperty(String propName) {
		String line;
		BufferedReader input = null;
		try {
			java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
			input = new BufferedReader(new InputStreamReader(p.getInputStream()), MemoryUnit.KB);
			line = input.readLine();
			input.close();
		} catch (IOException ex) {
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return line;
	}

	/**
	 * Determines the maximum possible number of times a view can completely fit horizontally across
	 * the device current screen width. This is useful for {@link android.support.v7.widget.GridLayoutManager}
	 * and {@link android.support.v7.widget.StaggeredGridLayoutManager} setting the span count
	 * in order to adapt easily across devices of varying sizes.
	 *
	 * @param context   {@link Context}
	 * @param viewPxWidth   Width of the view in pixels
	 * @return  Maximum number of times this view can be displayed horizontally without overlap
	 */
	public static int getMaxGridSpanCount(@NonNull Context context, @Px int viewPxWidth) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		return width / viewPxWidth;
	}

	/**
	 * Call to restart the application process using the apps launch intents
	 * <p>
	 * Behavior of the current process after invoking this method is undefined.
	 */
	public static void triggerRebirth(final Activity activity, final boolean fullRestart, final Class<?> cls, long restartDelay) {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				if (fullRestart) {
					activity.overridePendingTransition(0, 0);
					activity.startActivity(IntentUtils.getRestartIntent(activity));
					activity.overridePendingTransition(0, 0);
					activity.finish();
				} else {
					TaskStackBuilder.create(activity)
						.addNextIntentWithParentStack(new Intent(activity, cls))
						.startActivities();
				}
			}
		}, restartDelay);

		//Runtime.getRuntime().exit(0); // Kill kill kill!
	}

	public static Drawable getAttrDrawable(@NonNull Context context, @AttrRes int attr) {
		int[] attrs = new int[]{attr};
		TypedArray arr = context.obtainStyledAttributes(attrs);
		Drawable drawable = arr.getDrawable(0);
		arr.recycle();
		return drawable;
	}

	public static @ColorInt int getAttrColor(@NonNull Context context, @AttrRes int attr) {
		int[] attrs = new int[]{attr};
		TypedArray arr = context.obtainStyledAttributes(attrs);
		int color = arr.getColor(0, 0);
		arr.recycle();
		return color;
	}

	public static boolean isDeviceRooted() {
		String su = "su";
		String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/", "/system/bin/failsafe/",
			"/data/local/xbin/", "/data/local/bin/", "/data/local/"};
		for (String location : locations) {
			if (new File(location + su).exists()) {
				return true;
			}
		}
		return false;
	}

	public boolean copyToClipboard(@NonNull Context context, @StringRes int label, @StringRes int text) {
		return copyToClipboard(context, context.getString(label), context.getString(text));
	}

	public boolean copyToClipboard(@NonNull Context context, @NonNull String label, @NonNull String text) {
		ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard != null) {
			ClipData clip = ClipData.newPlainText(label, text);
			clipboard.setPrimaryClip(clip);
			return true;
		}
		return false;
	}

	@Nullable
	public String pasteFromClipboard(@NonNull Context context) {
		ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard != null) {
			ClipData clip = clipboard.getPrimaryClip();
			ClipData.Item item = clip.getItemAt(0);

			if (item != null) {
				CharSequence plainText = item.getText();
				if (plainText != null) {
					return plainText.toString();
				}

				Uri uriText = item.getUri();
				if (uriText != null) {
					return uriText.toString();
				}

				CharSequence coercedText = item.coerceToText(context);
				if (coercedText != null) {
					return coercedText.toString();
				}
			}
		}
		return null;
	}

	@Nullable
	public static Activity getActivityFromContext(@NonNull Context context) {
		if (context instanceof Activity) {
			return (Activity) context;
		} else if (context instanceof ContextWrapper) {
			return getActivityFromContext(((ContextWrapper) context).getBaseContext());
		} else {
			return null;
		}
	}

	public interface InputErrorCallback {
		boolean isInputValid(String entireText);
		String getErrorMessage();
	}

	public static void addErrorTextWatcher(@Nullable final TextInputLayout textInputLayout,
			@NonNull final EditText editText, @NonNull final InputErrorCallback callback, final boolean checkOnChange) {

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (textInputLayout != null) {
					textInputLayout.setErrorEnabled(false);
					textInputLayout.setError(null);
				} else {
					editText.setError(null);
				}

				if (checkOnChange) {
					performCheck(editText.getText().toString());
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (!checkOnChange) {
					performCheck(editText.getText().toString());
				}
			}

			private void performCheck(String entireText) {
				if (!callback.isInputValid(entireText)) {
					if (textInputLayout != null) {
						textInputLayout.setErrorEnabled(true);
						textInputLayout.setError(callback.getErrorMessage());
					} else {
						editText.setError(callback.getErrorMessage());
					}
				}
			}
		});

	}

}
