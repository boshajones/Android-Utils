package com.bosh.utils;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes units of memory, used within the {@link UnitUtils} class.
 *
 * @author David Jones
 * @version 1.0
 */
@IntDef({MemoryUnit.BYTE, MemoryUnit.KB, MemoryUnit.MB, MemoryUnit.GB})
@Retention(RetentionPolicy.SOURCE)
public @interface MemoryUnit {
	int BYTE = 1;
	int KB   = 1024;
	int MB   = 1048576;
	int GB   = 1073741824;
}
