package com.bosh.boshutils.base;

import android.support.v4.app.Fragment;

/**
 * Base Fragment containing methods useful to all {@link Fragment} implementations.
 */
public abstract class BaseFragment extends Fragment {

	/**
	 * Returns true when this {@link Fragment} is currently active, meaning that it is currently
	 * attached to an activity and isn't in the process of being removed. This check should be used
	 * when returning from a background thread to ensure the {@link Fragment} is safe to update.
	 */
	public boolean isFragmentActive() {
		return isAdded() && !isDetached() && !isRemoving();
	}
}
