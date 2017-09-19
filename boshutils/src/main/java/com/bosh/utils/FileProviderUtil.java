package com.bosh.utils;

import android.support.v4.content.FileProvider;

/**
 * Custom {@link FileProvider} used to prevent manifest {@link <provider>} collisions.
 * This class should not be directly used by an implementing project.
 *
 * {@see https://developer.android.com/guide/topics/manifest/provider-element.html}
 *
 * @author David Jones
 * @version 1.0
 */
public class FileProviderUtil extends FileProvider {
	// Class is left blank
}