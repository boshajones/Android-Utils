package com.bosh.boshutils;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by David Jones on 05/09/2017
 */
@SuppressWarnings({ "unused" })
public class KeyboardUtils {

	private static final int TAG_LISTENER_ID = 102593;

	/**
	 * Dispatches a request to hide the soft keyboard, removing it from the screen if possible
	 *
	 * @param context {@link Context}
	 * @param view {@link View} View used to retrieve window token
	 */
	public static void hideKeyboard(@NonNull Context context, @NonNull View view) {
		InputMethodManager imm = (InputMethodManager) context
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * Dispatches a request to show the soft keyboard if that's the users primary input
	 *
	 * @param context {@link Context}
	 * @param view {@link View} View used to show the keyboard
	 */
	public static void showKeyboard(@NonNull Context context, @NonNull View view) {
		InputMethodManager imm = (InputMethodManager) context
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * Determines if the soft keyboard is currently shown on screen, it returns true when there is a
	 * {@link View} active on an input method.
	 *
	 * @param context {@link Context}
	 * @return True if the keyboard is currently active
	 */
	public static boolean isKeyboardActive(@NonNull Context context) {
		InputMethodManager imm = (InputMethodManager) context
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm != null && imm.isActive();
	}

	/**
	 * Registers a listener for soft keyboard state changes, this will not trigger changes for
	 * hardware keyboards or if the user is using a keyboard shorter than 100px high.
	 *
	 * Listeners <b>must</b> be unregistered using the {@link #removeKeyboardListener(View)}
	 * method in order to remove the global layout listener.
	 *
	 * @param rootView Should be deepest full screen view, i.e. root of the layout passed to
	 * Activity.setContentView(...) or view returned by Fragment.onCreateView(...)
	 * @param listener Keyboard state listener
	 */
	public static void addKeyboardListener(@NonNull final View rootView,
		@NonNull final OnKeyboardShowListener listener) {

		final OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
			private boolean isKeyboardShown;
			private int initialHeightsDiff = -1;

			@Override
			public void onGlobalLayout() {
				final Rect frame = new Rect();
				rootView.getWindowVisibleDisplayFrame(frame);

				int heightDiff = rootView.getRootView().getHeight() - (frame.bottom - frame.top);
				if (initialHeightsDiff == -1) {
					initialHeightsDiff = heightDiff;
				}
				heightDiff -= initialHeightsDiff;

				if (heightDiff > 100) { // If more than 100 pixels, its probably a keyboard...
					if (!isKeyboardShown) {
						isKeyboardShown = true;
						listener.onKeyboardShow(true);
					}
				} else if (heightDiff < 50) {
					if (isKeyboardShown) {
						isKeyboardShown = false;
						listener.onKeyboardShow(false);
					}
				}
			}
		};

		rootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		rootView.setTag(TAG_LISTENER_ID, layoutListener);
	}

	/**
	 * Remove keyboard listener.
	 *
	 * @param rootView the root view
	 */
	public static void removeKeyboardListener(@NonNull View rootView) {
		final OnGlobalLayoutListener layoutListener =
			(OnGlobalLayoutListener) rootView.getTag(TAG_LISTENER_ID);
		rootView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
	}


	/**
	 * The interface On keyboard show listener.
	 */
	public interface OnKeyboardShowListener {

		/**
		 * On keyboard show.
		 *
		 * @param show the show
		 */
		void onKeyboardShow(boolean show);
	}

}
