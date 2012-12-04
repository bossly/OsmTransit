package com.bossly.lviv.transit.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.bossly.lviv.transit.fragments.NearRoutesFragment;

public class NearRoutesActivity extends FragmentActivity
{
	public static final String PREF_AGREEMENT_AGREE_V1 = "agreement_user_agree_v1.0";

	private NearRoutesFragment fragmentNear;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		fragmentNear = new NearRoutesFragment();

		transaction.add(android.R.id.content, fragmentNear);
		transaction.commit();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		fragmentNear.startDetermineUserLocation();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		fragmentNear.stopDetermineUserLocation();
	}
}