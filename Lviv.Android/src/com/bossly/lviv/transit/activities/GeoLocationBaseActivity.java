package com.bossly.lviv.transit.activities;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.bossly.lviv.transit.R;

public abstract class GeoLocationBaseActivity extends ActionBarActivity
		implements LocationListener {

	private LocationManager m_manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
		m_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// startDetermineUserLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// stopDetermineUserLocation();
	}

	public final void startDetermineUserLocation() {

		if (m_manager != null) {
			if (m_manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						0, 100, this);

				Location location = m_manager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				onLocationUpdated(location);
			} else {
				// notify about GPS if off
				Builder builder = new Builder(this);
				builder.setTitle(getString(R.string.dlg_location_title));
				builder.setMessage(R.string.dlg_location_message);

				builder.setPositiveButton(R.string.dlg_settings,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								startActivityForResult(
										new Intent(
												android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
										0);
							}
						});

				builder.setNegativeButton(android.R.string.cancel, null);
				builder.show();
			}

			if (m_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				m_manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 100, this);

				Location location = m_manager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

				onLocationUpdated(location);
			} else {
				// notify user about network location feature
			}
		}
	}

	public final void stopDetermineUserLocation() {
		if (m_manager != null) {
			m_manager.removeUpdates(this);
		}
	}

	/* LocationListener */

	@Override
	public final void onLocationChanged(Location location) {
		onLocationUpdated(location);
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

	/* Abstract methods */

	protected abstract void onLocationUpdated(Location location);
}
