package com.bossly.lviv.transit.services;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bossly.lviv.transit.activities.DashboardActivity;

public class TransitService extends Service {

	private static final String PREF_LAST_UPDATE = "pref_last_date";

	public static final String PREF_WIFI_ONLY = "wifi_only";

	// routes update every day
	private static final long UPDATE_TIME = 24 * 60 * 60 * 1000;

	private SharedPreferences prefs;

	private Date m_lastUpdateDate;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		long last_date = prefs.getLong(PREF_LAST_UPDATE, 0);
		m_lastUpdateDate = (last_date > 0) ? new Date(last_date) : new Date(0);

		long current = Calendar.getInstance().getTimeInMillis();

		boolean wifi_only = prefs.getBoolean(PREF_WIFI_ONLY, true);

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (current - m_lastUpdateDate.getTime() > UPDATE_TIME) {
			if ((wifi_only && mWifi.isConnected()) || !wifi_only) {
				// Do whatever
				Toast.makeText(this, "start update routes", Toast.LENGTH_LONG)
						.show();

				// check for updated and notify about it
				tryToUpdate();
			}
		}
	}

	private void tryToUpdate() {
		
		// check user option
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		boolean needCheck = prefs.getBoolean("autocheck", true);
		Date lastFromServlet;

		if (needCheck) {
			lastFromServlet = new Date(System.currentTimeMillis());
		} else {
			lastFromServlet = new Date(0);
		}

		boolean needDownloadData = true;

		File file = new File(getCacheDir(), "transit_data.zip");

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}
		// if cache exist - check if it latest version
		else {
			if (m_lastUpdateDate != null
					&& (System.currentTimeMillis() - m_lastUpdateDate.getTime()) < UPDATE_TIME) {

				// in server old data. no need to download data
				needDownloadData = false;
			}
		}

		if (needDownloadData && lastFromServlet != null) {

			prefs.edit().putLong(PREF_LAST_UPDATE, lastFromServlet.getTime())
					.commit();

			Intent intent = new Intent(getApplicationContext(),
					DashboardActivity.class);
			intent.putExtra("ppath", file.getAbsolutePath());
			intent.putExtra("download", true);
			intent.putExtra("version", lastFromServlet.getTime());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			boolean autoupdate = prefs.getBoolean("autoupdate", true);

			if (autoupdate) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}

		stopSelf();
	}
}
