package com.bossly.lviv.transit.activities;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.bossly.lviv.transit.fragments.ResultRoutesFragment;

public class SearchRouteActivity extends GeoLocationBaseActivity {

	public static final double MAX_DISTANCE = 300; // meters
	public static final String TAG_FRAGMENT = "tag_routes_fragment";

	private ResultRoutesFragment fragment;
	private Geocoder coder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			fragment = new ResultRoutesFragment();
			transaction.add(android.R.id.content, fragment, TAG_FRAGMENT);
			transaction.commit();
		} else {
			fragment = (ResultRoutesFragment) getSupportFragmentManager()
					.findFragmentByTag(TAG_FRAGMENT);
		}

		coder = new Geocoder(this);
		String locationName = getIntent().getStringExtra(Intent.EXTRA_SUBJECT);

		if (getIntent().getAction() == Intent.ACTION_VIEW) {
			String data = getIntent().getDataString();
			int index = data.indexOf("q=");
			locationName = data.substring(index + 2);
		}

		if (locationName != null) {// && coder.isPresent()) {

			try {
				List<Address> result = coder.getFromLocationName(locationName,
						1);

				if (result.size() > 0) {
					Address r = result.get(0);

					fragment.setDestination(r);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
