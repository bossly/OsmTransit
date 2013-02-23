package com.bossly.lviv.transit.services;

import org.mapsforge.core.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.GeoUtils.Point2D;
import com.bossly.lviv.transit.R;

public class TrackingService extends Service implements LocationListener
{

	private static final double MIN_DISTANCE = 500; // meters

	private LocationManager mLM;

	private NotificationManager mNM;

	// private GeoPoint[] points;

	private GeoPoint pointB;

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);

		double lat = intent.getExtras().getDouble("lat");
		double lon = intent.getExtras().getDouble("lon");

		pointB = new GeoPoint(lat, lon);

		Boolean status = intent.getBooleanExtra("status", false);

		if (status)
		{
			stopSelf();
		}
		else
		{
			mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, this);
			mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, this);

			showTrackingNotification();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		mNM.cancel(0);

		mLM.removeUpdates(this);
	}

	private void showTrackingNotification()
	{
		// In this sample, we'll use the same text for the
		// ticker and the expanded notification
		CharSequence text = getString(R.string.srv_warning_title);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_stat_updates, text,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_NO_CLEAR;

		Intent intent = new Intent(this, TrackingService.class);
		intent.putExtra("status", true);
		PendingIntent contentIntent = PendingIntent.getService(this, 0, intent, 0);

		// Set the info for the views that show in the
		// notification panel.
		notification.setLatestEventInfo(this, getString(R.string.srv_warning_msg), text, contentIntent);

		// Send the notification.
		mNM.notify(0, notification);
	}

	/** Show a notification while this service is running. */
	private void showNotification(Intent intent)
	{
		// In this sample, we'll use the same text for the
		// ticker and the expanded notification
		CharSequence text = getString(R.string.srv_warning_exit_title);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_stat_updates, text,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// Set the info for the views that show in the
		// notification panel.
		notification.setLatestEventInfo(this, getString(R.string.srv_warning_exit_msg), text,
				contentIntent);

		// Send the notification.
		mNM.notify(1, notification);

		stopSelf();
	}

	/* LocationListener */

	Location m_location = null;

	double meters = 0;

	@Override
	public void onLocationChanged(Location location)
	{
		if (location != null && GeoUtils.isBetterLocation(location, m_location, 2 * 60 * 1000))
		{
			m_location = location;

			Intent intent = new Intent();
			// getApplicationContext(), DashboardActivity.class );
			// intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );

			meters = Point2D.distance(location.getLatitude(), location.getLongitude(), pointB.getLatitude(),
					pointB.getLongitude());

			if (meters < MIN_DISTANCE)
			{
				showNotification(intent);
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}
}
