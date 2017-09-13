package com.bosh.utils.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import com.bosh.utils.R;
import java.util.Set;

/**
 * Base Activity containing methods common to all {@link Activity} objects in the application, including
 * swapping the current fragment, and handling any {@link Fragment} objects that wish to intercept
 * the Android back button using the {@link OnBackPressedListener}
 *
 * @author hedgehog lab
 */
public abstract class BaseActivity extends AppCompatActivity {

	@AnimRes private static final int ENTER_ANIMATION = R.anim.slide_in_from_right;
	@AnimRes private static final int EXIT_ANIMATION = R.anim.fade_out;
	@AnimRes private static final int POP_ENTER_ANIMATION = R.anim.fade_in;
	@AnimRes private static final int POP_EXIT_ANIMATION = R.anim.slide_out_to_right;

	/**
	 * Classes that implement the OnBackPressedListener wish to intercept the Android system back
	 * button from the activity. Implementing classes should return true if the back event was consumed,
	 * else false.
	 *
	 * @author hedgehog lab
	 */
	public interface OnBackPressedListener {
		boolean onBackPressed();
	}

	/**
	 * Overrides the Android back button and checks to see if the current fragment implements
	 * {@link OnBackPressedListener} if so the {@link OnBackPressedListener#onBackPressed()} method is
	 * called so the {@link Fragment} can take action. If the {@link Fragment} does not consume the
	 * back pressed event, by returning false, super is called which takes care of popping the
	 * fragment backstack or finishing the activity as appropriate.
	 */
	@Override
	public void onBackPressed() {
		boolean handled = false;

		Fragment fragment = getSupportFragmentManager().findFragmentById(getCurrentFragmentId());
		if (fragment != null && fragment instanceof OnBackPressedListener) {
			handled = ((OnBackPressedListener) fragment).onBackPressed();
		}

		if (!handled) {
			super.onBackPressed();
		}
	}

	protected void swapFragment(@NonNull Fragment fragment, boolean addToBackStack, boolean animate) {
		swapFragment(fragment, getCurrentFragmentId(), addToBackStack, animate);
	}

	/**
	 * Finds a container with the containerViewId 'R.id.content_fragment' and replaces any content
	 * with the provided {@link Fragment} instance. It handles checking to ensure that the provided
	 * {@link Fragment}is not identical to the current {@link Fragment}, checking bundles if necessary.
	 *
	 * @param fragment	{@link Fragment} to load into the container
	 * @param addToBackStack	True if the {@link Fragment} should be added to the activity backstack
	 * @param animate	True if the {@link Fragment} should animate into and out of view
	 */
	protected void swapFragment(@NonNull Fragment fragment, @IdRes int contentFragmentId,
			boolean addToBackStack, boolean animate) {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Fragment currentFragment = fragmentManager.findFragmentById(contentFragmentId);
		if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
			// Check the fragment arguments, if the arguments are the same then yield
			if (areBundlesEqual(currentFragment.getArguments(), fragment.getArguments())) {
				return;
			}
		}

		if (animate) {
			int[] anims = getAnimationIntArray();
			fragmentTransaction.setCustomAnimations(anims[0], anims[1], anims[2], anims[3]);
		}

		fragmentTransaction.replace(contentFragmentId, fragment);

		if (addToBackStack) {
			fragmentTransaction.addToBackStack(null);
		}

		fragmentTransaction.commit();
	}

	/**
	 * Returns true if the provided {@link Bundle} objects are equal. This is done by first checking
	 * for nullity, then checking the sizes of the bundles and yielding if they're not equal.
	 *
	 * If the bundle arguments contain further bundle objects then recursion is used, otherwise as a
	 * value for a key can be null, both values are checked in both bundles and then compared.
	 *
	 * @param args1	{@link Bundle} Arguments to compare
	 * @param args2	{@link Bundle} Arguments to compare
	 * @return	True if both bundles are equivalent, else false
	 */
	protected boolean areBundlesEqual(@Nullable Bundle args1, @Nullable Bundle args2) {
		if (args1 == null && args2 == null) {
			return true;
		} else if (args1 == null) {
			return false;
		} else if (args2 == null) {
			return false;
		}

		if (args1.size() != args2.size()) {
			return false;
		}

		Set<String> setOne = args1.keySet();
		Object valueOne;
		Object valueTwo;

		for(String key : setOne) {
			valueOne = args1.get(key);
			valueTwo = args2.get(key);
			if(valueOne instanceof Bundle && valueTwo instanceof Bundle &&
				!areBundlesEqual((Bundle) valueOne, (Bundle) valueTwo)) {
				return false;
			} else if(valueOne == null) {
				if(valueTwo != null || !args2.containsKey(key)) {
					return false;
				}
			} else if(!valueOne.equals(valueTwo)) {
				return false;
			}
		}

		return true;
	}

	protected Fragment getCurrentFragment(FragmentManager fragmentManager) {
		return fragmentManager.findFragmentById(getCurrentFragmentId());
	}

	protected int[] getAnimationIntArray() {
		return new int[]{ENTER_ANIMATION, EXIT_ANIMATION, POP_ENTER_ANIMATION, POP_EXIT_ANIMATION};
	}

	protected abstract int getCurrentFragmentId();
}
