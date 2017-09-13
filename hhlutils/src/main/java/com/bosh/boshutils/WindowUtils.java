package com.bosh.boshutils;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import java.lang.reflect.Method;

/**
 * Created by David Jones on 12/09/2017
 */
@SuppressWarnings({ "unused" })
public class WindowUtils {

	private static final int TAG_OFFSET = 1284;

	public static @Px int getStatusBarHeight(@NonNull Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
		return resources.getDimensionPixelSize(resourceId);
	}

	public static @Px int getToolbarHeight(@NonNull final Activity activity) {
		TypedValue tv = new TypedValue();
		if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
		}
		return 0;
	}

	public static @Px int getNavigationBarHeight(@NonNull Context context) {
		Resources res = context.getResources();
		int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId != 0) {
			return res.getDimensionPixelSize(resourceId);
		} else {
			return 0;
		}
	}

	public static void addStatusBarMarginToView(@NonNull View view) {
		if (Build.VERSION.SDK_INT < VERSION_CODES.KITKAT) return;

		Object haveSetOffset = view.getTag(TAG_OFFSET);
		if (haveSetOffset != null && (Boolean) haveSetOffset) {
			// If status bar margin has already been applied to this view, yield
			return;
		}

		ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		layoutParams.setMargins(layoutParams.leftMargin,
			layoutParams.topMargin + getStatusBarHeight(view.getContext()),
			layoutParams.rightMargin, layoutParams.bottomMargin);
		view.setTag(TAG_OFFSET, true);
	}

	public static void removeStatusBarMarginFromView(@NonNull View view) {
		if (Build.VERSION.SDK_INT < VERSION_CODES.KITKAT) return;

		Object haveSetOffset = view.getTag(TAG_OFFSET);
		if (haveSetOffset == null || !(Boolean) haveSetOffset) {
			// If status bar margin hasn't been applied to this view, yield
			return;
		}
		ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		layoutParams.setMargins(layoutParams.leftMargin,
			layoutParams.topMargin - getStatusBarHeight(view.getContext()),
			layoutParams.rightMargin, layoutParams.bottomMargin);
		view.setTag(TAG_OFFSET, false);
	}

	/**
	 * Requests a translucent status bar for the provided activities window with minimal
	 * system-provided background protection.
	 *
	 * @param activity	{@link Activity}
	 * @param translucent	True for a translucent status bar, else false
	 */
	protected void setStatusBarTranslucent(@NonNull Activity activity, boolean translucent) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			Window win = activity.getWindow();
			WindowManager.LayoutParams winParams = win.getAttributes();
			final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			if (translucent) {
				winParams.flags |= bits;
			} else {
				winParams.flags &= ~bits;
			}
			win.setAttributes(winParams);
		}
	}

	/**
	 * Updates the provided activities window status bar to the provided colour resource
	 *
	 * @param activity  {@link Activity}
	 * @param color     Colour resource to set the status bar colour to
	 */
	public static void setStatusBarColor(@NonNull Activity activity, @ColorRes int color) {
		if (CommonUtils.isMiUi()) return;

		if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
			Window window = activity.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(ContextCompat.getColor(activity, color));
		} else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.BLACK);
		}
	}

	public static void setLightStatusBar(View view, @NonNull Activity activity) {
		if (CommonUtils.isMiUi()) return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int flags = view.getSystemUiVisibility();
			flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			view.setSystemUiVisibility(flags);
			activity.getWindow().setStatusBarColor(Color.WHITE);
		}
	}

	public static void clearLightStatusBar(@NonNull View view) {
		if (CommonUtils.isMiUi()) return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int flags = view.getSystemUiVisibility();
			flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			view.setSystemUiVisibility(flags);
		}
	}

	public static void setNavigationBarColor(@NonNull Activity activity, @ColorRes int color) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();
			window.setNavigationBarColor(ContextCompat.getColor(activity, color));
		}
	}

	public static void hideNavigationBar(@NonNull final Activity activity) {
		View decorView = activity.getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			uiOptions = uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}
		decorView.setSystemUiVisibility(uiOptions);
	}

	@RequiresPermission(permission.EXPAND_STATUS_BAR)
	public static void openSystemNotifications(@NonNull final Context context) {
		String methodName = (Build.VERSION.SDK_INT <= 16) ? "expand" : "expandNotificationsPanel";
		invokeStatusBar(context, methodName);
	}

	@RequiresPermission(permission.EXPAND_STATUS_BAR)
	public static void closeSystemNotifications(@NonNull final Context context) {
		String methodName = (Build.VERSION.SDK_INT <= 16) ? "collapse" : "collapsePanels";
		invokeStatusBar(context, methodName);
	}

	@SuppressLint("PrivateApi, WrongConstant")
	@RequiresPermission(permission.EXPAND_STATUS_BAR)
	private static void invokeStatusBar(@NonNull final Context context, @NonNull final String methodName) {
		try {
			Object service = context.getSystemService("statusbar");
			Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
			Method expand = statusBarManager.getMethod(methodName);
			expand.invoke(service);
		} catch (Exception e) {
			UtilLogger.e("WindowUtils", "Exception invoking status bar manager", e);
		}
	}
}
