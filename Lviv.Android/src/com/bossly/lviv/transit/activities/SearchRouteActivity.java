package com.bossly.lviv.transit.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.fragments.ResultRoutesFragment;

public class SearchRouteActivity extends GeoLocationBaseActivity {
	private ResultRoutesFragment fragment;

	/*
	 * This activity should process: geo:latitude,longitude
	 * geo:latitude,longitude?z=zoom geo:0,0?q=my+street+address
	 * geo:0,0?q=business+near+city
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			fragment = new ResultRoutesFragment();
			transaction.add(android.R.id.content, fragment, fragment.getClass()
					.getName());
			transaction.commit();
		} else {
			fragment = (ResultRoutesFragment) getSupportFragmentManager()
					.findFragmentByTag(fragment.getClass().getName());
		}

		String locationName = getIntent().getStringExtra(Intent.EXTRA_SUBJECT);

		if (getIntent().getAction() == Intent.ACTION_VIEW) {
			Location loc = GeoUtils.parseGeo(locationName);
			fragment.setDestination(loc);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		startDetermineUserLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();

		stopDetermineUserLocation();
	}

	@Override
	protected void onLocationUpdated(Location location) {
		fragment.onLocationUpdated(location);
	}
}
