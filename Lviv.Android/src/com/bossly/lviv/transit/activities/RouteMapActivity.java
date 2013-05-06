package com.bossly.lviv.transit.activities;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.fragments.ResultRoutesFragment;

public class RouteMapActivity extends FragmentActivity implements
		LocationListener {

	public static final double MAX_DISTANCE = 300; // meters

	ResultRoutesFragment fragment = new ResultRoutesFragment();
	private LocationManager m_manager;
	private Geocoder coder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);

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

		m_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

	public void startDetermineUserLocation() {

		if (m_manager != null) {
			if (m_manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						0, 100, this);

				Location location = m_manager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				fragment.onLocationUpdated(location);
			} else {
				// notify about GPS if off
				/*
				 * Builder builder = new Builder( this ); builder.setTitle(
				 * getString( R.string.dlg_location_title ) );
				 * builder.setMessage( R.string.dlg_location_message );
				 * 
				 * builder.setPositiveButton( R.string.dlg_settings, new
				 * DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick( DialogInterface dialog, int
				 * which ) { startActivityForResult( new Intent(
				 * android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS ),
				 * 0 ); } } );
				 * 
				 * builder.setNegativeButton( android.R.string.cancel, null );
				 * 
				 * builder.show();
				 */
			}

			if (m_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				m_manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 100, this);

				Location location = m_manager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

				fragment.onLocationUpdated(location);
			} else {
				// notify user about network location feature
			}
		}
	}

	public void stopDetermineUserLocation() {
		if (m_manager != null) {
			m_manager.removeUpdates(this);
		}
	}

	/* LocationListener */

	@Override
	public void onLocationChanged(Location location) {
		fragment.onLocationUpdated(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
