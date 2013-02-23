package com.bossly.lviv.transit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;

public final class CommonUtils
{
	public static CharSequence highlight(String text, String[] words, int color)
	{
		return highlight(text, words, color, Locale.getDefault());
	}

	public static CharSequence highlight(String text, String[] words, int color, Locale locale)
	{
		SpannableString builder = new SpannableString(text);

		text = text.toLowerCase(locale);

		for (int i = 0; i < words.length; i++)
		{
			String word = words[i].toLowerCase(locale);

			if (TextUtils.isEmpty(word))
				throw new IllegalArgumentException("words");

			for (int p = text.indexOf(word); p != -1; p = text.indexOf(word, p + 1))
			{
				builder.setSpan(new BackgroundColorSpan(color), p, p + word.length(), 0);
			}
		}

		return builder;
	}

	public static void copyData(InputStream in, OutputStream out) throws IOException
	{
		copyData(in, out, 1024);
	}

	public static void copyData(InputStream in, OutputStream out, int bufferSize) throws IOException
	{
		byte[] buf = new byte[bufferSize];
		int length = 0;

		while ((length = in.read(buf)) > 0)
		{
			out.write(buf, 0, length);
		}
	}
}
