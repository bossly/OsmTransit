package com.bossly.lviv.transit.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.activities.RoutesActivity;
import com.bossly.lviv.transit.data.DatabaseSource;
import com.bossly.osm.transit.Node;
import com.bossly.osm.transit.Region;
import com.bossly.osm.transit.Route;
import com.bossly.osm.transit.WebAPI;
import com.bossly.lviv.transit.data.RoutesContract;

public class TransitService extends IntentService {
	private final static String URL_FORMAT = "http://overpass-api.de/api/interpreter?data=relation(%s)%s%s";

	Region Bounds_Lviv = new Region(49.7422316, 23.8623047, 49.9529871,
			24.2056274);
	// big lviv - 49.67, 23.774, 50.004, 24.291
	Region Bounds_Kyiv = new Region(50.281, 30.263, 50.61, 30.81);
	Region Bounds_Kharkiv = new Region(49.9022, 36.1309, 50.0684, 36.3895);

	Region currentRegion = Bounds_Lviv;

	private final static String ROUTE_TAGS = "[\"route\"~\"trolleybus|tram|bus\"];>>;";
	private final static String ROUTE_META = Uri.encode("out meta;");

	public static final int UPDATE_NOTIFICATION_ID = 0x01;
	public final static String ACTION_UPDATED = "TransitService.updated";

	private boolean mIsRunning = false;

	public TransitService() {
		super("TransitService");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (mIsRunning) {
			Toast.makeText(this, "Updating is already started",
					Toast.LENGTH_SHORT).show();
		} else {
			super.onStart(intent, startId);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mIsRunning = true;
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationBuilder.setSmallIcon(R.drawable.ic_update_service);
		notificationBuilder.setContentTitle(getString(R.string.app_name));
		notificationBuilder
				.setContentText(getString(R.string.title_data_loading));
		notificationBuilder.setAutoCancel(false);
		notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0,
				new Intent(this, RoutesActivity.class), 0));

		notificationBuilder.setProgress(0, 0, true);

		Notification n = notificationBuilder.build();

		n.flags |= Notification.FLAG_NO_CLEAR;

		notificationManager.notify(UPDATE_NOTIFICATION_ID, n);

		String link = String.format(URL_FORMAT, currentRegion.toString(),
				ROUTE_TAGS, ROUTE_META);
		ArrayList<Route> routes = null;

		try {
			routes = new WebAPI().parseTransitInfoByUrl(new URL(link));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		if (routes != null) {
			// save to db
			DatabaseSource db = new DatabaseSource(this);
			db.open();
			db.beginTransaction();
			db.clear();
			int added = 0;
			int index = 0;

			ContentValues pointValues = new ContentValues();

			for (Route route : routes) {

				// ignore routes out of city
				if (route.insideCity(currentRegion.Top, currentRegion.Left,
						currentRegion.Bottom, currentRegion.Right) && route.name != null) {
					
					long routeId = db.insertRoute(route.id, route.getName(),
							route.route, route.genDescription(),
							route.genPath());

					if (routeId == -1)
						Log.e(RoutesActivity.class.getName(), "Can't add item");

					for (Node node : route.getNodes()) {
						pointValues.put(RoutesContract.PointData.ROUTE_ID,
								routeId);
						pointValues.put(RoutesContract.PointData.LATITUDE,
								node.lat);
						pointValues.put(RoutesContract.PointData.LONGITUDE,
								node.lon);

						db.insertNode(pointValues);
					}

					added++;

					notificationBuilder.setProgress(routes.size(), index++,
							false);
					notificationManager.notify(UPDATE_NOTIFICATION_ID,
							notificationBuilder.build());
				}
			}

			Log.d(RoutesActivity.class.getName(), "Routes addded to db: "
					+ added);

			db.endTransaction();
			db.close();
		}

		notificationManager.cancel(UPDATE_NOTIFICATION_ID);

		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(ACTION_UPDATED));
		mIsRunning = false;
	}
}
