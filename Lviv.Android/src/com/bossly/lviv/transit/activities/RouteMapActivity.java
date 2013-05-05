package com.bossly.lviv.transit.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bossly.lviv.transit.CoreApplication;
import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.RouteAdapter;
import com.bossly.lviv.transit.data.DatabaseSource;

public class RouteMapActivity extends ListActivity implements LocationListener {

	public static final double MAX_DISTANCE = 300; // meters

	private Location m_location = null;
	private Location m_locationTo = null;

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

					m_locationTo = new Location("Unknown");
					m_locationTo.setLatitude(r.getLatitude());
					m_locationTo.setLongitude(r.getLongitude());

					TextView tv = (TextView) findViewById(R.id.textTo);
					tv.setText("до: " + fromAddress(r));
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

				onLocationUpdated(location);
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

				onLocationUpdated(location);
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

	private void findRoutesFromTo() {
		if (m_location != null && m_locationTo != null) {

			ArrayList<Route> data = CoreApplication.get(RouteMapActivity.this).data;
			
			if(data == null)
			{
				DatabaseSource db = new DatabaseSource(this);
				db.open();
				data = db.getRoutes();
				db.close();
			}

			if (data == null)
				return;

			ArrayList<Route> f1 = GeoUtils.filterRoutes(data,
					m_location.getLatitude(), m_location.getLongitude(),
					MAX_DISTANCE);
			ArrayList<Route> f2 = GeoUtils.filterRoutes(f1,
					m_locationTo.getLatitude(), m_locationTo.getLongitude(),
					MAX_DISTANCE);

			Log.d("Routes founded:", "" + f2.size());

			RouteAdapter adapter = new RouteAdapter(this, f2);
			setListAdapter(adapter);
		}
	}

	private String fromAddress(Address adr) {
		StringBuilder b = new StringBuilder();

		for (int i = 0; i < 10; i++) {
			String line = adr.getAddressLine(i);

			if (line == null)
				break;

			b.append(line + ", ");
		}

		return b.toString();
	}

	private void onLocationUpdated(Location location) {

		if (location != null
				&& GeoUtils.isBetterLocation(location, m_location, TWO_MINUTES)) {

			// if (coder.isPresent())
			{
				try {
					List<Address> result = coder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);

					if (result.size() > 0) {
						Address r = result.get(0);
						TextView tv = (TextView) findViewById(R.id.textFrom);
						tv.setText("від: " + fromAddress(r));
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			m_location = location;
			findRoutesFromTo();
		}
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/* LocationListener */

	@Override
	public void onLocationChanged(Location location) {
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
}
