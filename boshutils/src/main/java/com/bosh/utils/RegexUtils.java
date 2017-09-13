package com.bosh.utils;

import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

/**
 * Created by David Jones on 12/09/2017
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RegexUtils {

	public static boolean isGenericPhoneNumber(@Nullable String phoneNumber) {
		phoneNumber = removeNonDigits(phoneNumber);

		return !StringUtils.isEmpty(phoneNumber) &&
			PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber);
	}

	public static boolean isSimplePhoneNumber(@Nullable String phoneNumber) {
		final String SIMPLE_MOBILE_REGEX = "^[1]\\d{10}$";

		return !StringUtils.isEmpty(phoneNumber) && Pattern.matches(SIMPLE_MOBILE_REGEX, phoneNumber);
	}

	public static boolean isStrictPhoneNumber(@Nullable String phoneNumber) {
		final String STRICT_MOBILE_REGEX  = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,1,3,5-8])|(18[0-9])|(147))\\d{8}$";

		return !StringUtils.isEmpty(phoneNumber) && Pattern.matches(STRICT_MOBILE_REGEX, phoneNumber);
	}

	@Nullable
	private static String removeNonDigits(@Nullable String string) {
		if (string == null) return null;

		return string.replaceAll("\\D+","");
	}

	public static boolean isEmailAddress(@Nullable String emailAddress) {
		return !StringUtils.isEmpty(emailAddress) &&
			Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
	}

	public static boolean isUrl(@Nullable String url) {
		return !StringUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
	}

}
