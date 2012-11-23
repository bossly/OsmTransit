package com.bossly.lviv.transit.utils;

import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

public final class CommonUtils
{
	public static CharSequence highlight(String text, String[] words, int color)
	{
		SpannableString builder = new SpannableString(text);
		BackgroundColorSpan spanColor = new BackgroundColorSpan(color);
		
		text = text.toLowerCase();
		
		for (int i = 0; i < words.length; i++)
    {
			String word = words[0].toLowerCase();  
			
			for (int p = text.indexOf(word); p != -1; p = text.indexOf(word, p + 1))
	    {
				builder.setSpan(spanColor, p, p + word.length(), 0);			
	    }
    }
		
		return builder;
	}
}
