package com.bossly.lviv.transit.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.bossly.lviv.transit.R;

public class SettingsActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
