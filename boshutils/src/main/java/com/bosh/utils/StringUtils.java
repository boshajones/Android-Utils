package com.bosh.utils;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Handles common String related tasks, such as checking if the string is empty or null, removing
 * non alphanumeric characters, and getting the height of the displayed text in pixels. This class
 * also contains methods for adding and creating text {@link Spannable}s for features like coloured,
 * bold, underlined, and clickable text.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class StringUtils {

	/**
	 * Returns true if the provided {@link EditText} or a subclass thereof currently doesn't contain any text
	 *
	 * @param editText	{@link EditText} or subclass to check
	 * @return	True if there is no text
	 */
	public static boolean isEmpty(@Nullable EditText editText) {
		return editText == null || isEmpty(editText.getText().toString());
	}

	/**
	 * Returns true if the provided {@link String} is empty, after discarding any whitespace
	 *
	 * @param string	{@link String} to check
	 * @return	True if there is no text
	 */
	public static boolean isEmpty(@Nullable String string) {
		return string == null || string.trim().length() <= 0;
	}

	public static String cleanString(String string) {
		return string.replace("\n", "").replace("\r", "")
			.replaceAll("[^a-zA-Z0-9]", "").trim();
	}

	@Nullable
	public static String removeNonDigits(@Nullable String string) {
		if (string == null) return null;

		return string.replaceAll("\\D+","");
	}

	public static int getTextHeight(@NonNull TextView textView) {
		String text = textView.getText().toString();

		Rect bounds = new Rect();
		Paint textPaint = textView.getPaint();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		return bounds.height();
	}

	public static int getTextWidth(@NonNull TextView textView) {
		String text = textView.getText().toString();

		Rect bounds = new Rect();
		Paint textPaint = textView.getPaint();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		return bounds.width();
	}

	public static int[] getTextPosition(@NonNull String entireText, @NonNull String selectedText) {
		int[] pos = new int[2];
		pos[0] = entireText.indexOf(selectedText);
		pos[1] = pos[0] + selectedText.length();
		return pos;
	}

	public static Spannable createBoldSpan(@NonNull String entireText, @NonNull String boldText) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(entireText);
		return addBoldSpan(ssb, entireText, boldText);
	}

	public static Spannable addBoldSpan(@NonNull Spannable spannable, @NonNull String entireText,
			@NonNull String... boldText) {

		for (String bold : boldText) {
			int[] pos = getTextPosition(entireText, bold);
			spannable.setSpan(new StyleSpan(Typeface.BOLD), pos[0], pos[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	public static Spannable createUnderlinedSpan(String entireText, String selectedText) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(entireText);
		return addUnderlineSpan(ssb, entireText, selectedText);
	}

	public static Spannable addUnderlineSpan(@NonNull Spannable spannable, @NonNull String entireText,
			@NonNull String... underlineText) {

		for (String underline : underlineText) {
			int[] pos = getTextPosition(entireText, underline);
			spannable.setSpan(new UnderlineSpan(), pos[0], pos[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	public static Spannable createStyleSpan(int style, @NonNull String entireText, @NonNull String selectedText) {
		SpannableString spannableContent = new SpannableString(entireText);
		return addStyleSpan(spannableContent, style, entireText, selectedText);
	}

	public static Spannable addStyleSpan(@NonNull Spannable spannable, int style, @NonNull String entireText, @NonNull String... styleText) {

		for (String s : styleText) {
			int[] pos = getTextPosition(entireText, s);
			spannable.setSpan(new StyleSpan(style), pos[0], pos[1], 0);
		}
		return spannable;
	}

	public static Spannable createForegroundColorSpan(int color, @NonNull String entireText, @NonNull String selectedText) {
		SpannableString spannableContent = new SpannableString(entireText);
		return addForegroundColorSpan(spannableContent, color, entireText, selectedText);
	}

	public static Spannable addForegroundColorSpan(@NonNull Spannable spannable, @ColorInt int color,
			@NonNull String entireText, @NonNull String... colorText) {

		for (String s : colorText) {
			int[] pos = getTextPosition(entireText, s);
			spannable.setSpan(new ForegroundColorSpan(color), pos[0], pos[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	public static Spannable createClickableSpan(@NonNull ClickableSpan span, @NonNull String entireText, @NonNull String selectedText) {
		SpannableString spannableContent = new SpannableString(entireText);
		return addClickableSpan(spannableContent, span, entireText, selectedText);
	}

	public static Spannable addClickableSpan(@NonNull Spannable spannable, @NonNull ClickableSpan span,
			@NonNull String entireText, @NonNull String... clickableText) {

		for (String s : clickableText) {
			int[] pos = getTextPosition(entireText, s);
			spannable.setSpan(span, pos[0], pos[1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	public static Spannable createTypefaceSpan(@NonNull String family, @NonNull Typeface type,
			@NonNull String entireText, @NonNull String selectedText) {

		SpannableString spannableContent = new SpannableString(entireText);
		return addTypefaceSpan(spannableContent, family, type, entireText, selectedText);
	}

	public static Spannable addTypefaceSpan(@NonNull Spannable spannable, @NonNull String family,
			@NonNull Typeface type, @NonNull String entireText, @NonNull String... selectedText) {

		CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan(family, type);
		for (String s : selectedText) {
			int[] pos = getTextPosition(entireText, s);
			spannable.setSpan(typefaceSpan, pos[0], pos[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	public static class CustomTypefaceSpan extends TypefaceSpan {

		private final Typeface newType;

		public CustomTypefaceSpan(@NonNull String family, @NonNull Typeface type) {
			super(family);
			newType = type;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			applyCustomTypeFace(ds, newType);
		}

		@Override
		public void updateMeasureState(TextPaint paint) {
			applyCustomTypeFace(paint, newType);
		}

		private static void applyCustomTypeFace(Paint paint, Typeface tf) {
			int oldStyle;
			Typeface old = paint.getTypeface();
			if (old == null) {
				oldStyle = 0;
			} else {
				oldStyle = old.getStyle();
			}

			int fake = oldStyle & ~tf.getStyle();
			if ((fake & Typeface.BOLD) != 0) {
				paint.setFakeBoldText(true);
			}

			if ((fake & Typeface.ITALIC) != 0) {
				paint.setTextSkewX(-0.25f);
			}

			paint.setTypeface(tf);
		}
	}

}
