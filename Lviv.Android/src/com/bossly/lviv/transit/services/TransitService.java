package com.bossly.lviv.transit.services;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.activities.DashboardActivity;
import com.bossly.lviv.transit.data.DatabaseSource;
import com.bossly.lviv.transit.data.Main;

public class TransitService extends IntentService
{
	public static final int UPDATE_NOTIFICATION_ID = 0x01;
	public final static String ACTION_UPDATED = "TransitService.updated";
	
	private boolean mIsRunning = false;

	public TransitService()
	{
		super("TransitService");
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		if(mIsRunning)
		{
			Toast.makeText(this, "Updating is already statred", Toast.LENGTH_SHORT).show();
		}
		else
		{
			super.onStart(intent, startId);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		mIsRunning = true;
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification.Builder b;

		notificationBuilder.setSmallIcon(R.drawable.ic_update_service);
		notificationBuilder.setContentTitle(getString(R.string.app_name));
		notificationBuilder.setContentText(getString(R.string.title_data_loading));
		notificationBuilder.setAutoCancel(false);
		notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
				DashboardActivity.class), 0));

		notificationBuilder.setProgress(0, 0, true);
		
		Notification n = notificationBuilder.build();
		
		n.flags |= Notification.FLAG_NO_CLEAR;
		
		notificationManager.notify(UPDATE_NOTIFICATION_ID, n);

		ArrayList<com.bossly.lviv.transit.data.Route> routes = Main.LoadData(null);

		// save to db
		DatabaseSource db = new DatabaseSource(this);
		db.open();
		db.beginTransaction();
		db.clear();
		int added = 0;
		int index = 0;

		for (com.bossly.lviv.transit.data.Route route : routes)
		{
			// ignore routes out of city
			if (route.insideCity(49.7422316, 23.8623047, 49.9529871, 24.2056274))
			{
				if(db.insertRoute(route.id, route.name, route.route, route.genDescription(), route.genPath()) == -1)
					Log.e(DashboardActivity.class.getName(), "Can't add item");
				added++;

				notificationBuilder.setProgress(routes.size(), index++, false);
				notificationManager.notify(UPDATE_NOTIFICATION_ID, notificationBuilder.build());
			}
		}

		Log.d(DashboardActivity.class.getName(), "Routes addded to db: " + added);

		db.endTransaction();
		db.close();

		notificationManager.cancel(UPDATE_NOTIFICATION_ID);
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_UPDATED));
		mIsRunning = false;
	}
}
