package com.bosh.utils;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by David Jones on 12/09/2017
 */
@IntDef({MemoryUnit.BYTE, MemoryUnit.KB, MemoryUnit.MB, MemoryUnit.GB})
@Retention(RetentionPolicy.SOURCE)
public @interface MemoryUnit {
	int BYTE = 1;
	int KB   = 1024;
	int MB   = 1048576;
	int GB   = 1073741824;
}
