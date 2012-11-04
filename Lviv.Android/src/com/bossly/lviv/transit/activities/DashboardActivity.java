package com.bossly.lviv.transit.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.bossly.lviv.transit.CoreApplication;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.data.DatabaseSource;
import com.bossly.lviv.transit.data.Main;
import com.bossly.lviv.transit.services.TransitService;

public class DashboardActivity extends android.support.v4.app.FragmentActivity
		implements OnClickListener, LoaderCallbacks<List<Route>> {

	public static final String PREF_LAST_UPDATE = "pref_last_date";

	private SharedPreferences prefs;

	private Handler m_runMap = null;

	private View m_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dashboard);

		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		m_loading = findViewById(R.id.loading);

		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.button4).setOnClickListener(this);
		findViewById(R.id.button5).setOnClickListener(this);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (extras != null) {
			boolean download = extras.getBoolean("download", false);

			if (download) {
				Date version = new Date(extras.getLong("version"));
				String path = new File(getCacheDir(), "transit_data.zip")
						.getAbsolutePath();
				new UpdateAsyncTask(version).execute(path);
			} else {
				// Prepare the loader. Either re-connect with an
				// existing one, or start a new one.
				getSupportLoaderManager().initLoader(0, null, this);
			}
		} else {
			// Prepare the loader. Either re-connect with an
			// existing one, or start a new one.
			getSupportLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// start self-working service
		startService(new Intent(this, TransitService.class));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		getSupportLoaderManager().destroyLoader(0);
	}

	@Override
	public void onClick(View v) {

		CoreApplication app = (CoreApplication) getApplication();

		// NEAR
		if (v.getId() == R.id.button1) {
			final Intent i = new Intent(this, NearRoutesActivity.class);

			if (app.data == null) {
				m_runMap = new Handler() {
					public void handleMessage(android.os.Message msg) {
						startActivity(i);
					};
				};
			} else {
				startActivity(i);
			}
		}
		// MAP
		else if (v.getId() == R.id.button3) {
			if (app.data == null) {
				m_runMap = new Handler() {
					public void handleMessage(android.os.Message msg) {
						startActivity(new Intent(DashboardActivity.this,
								RouteMapActivity.class));
					};
				};
			} else {
				startActivity(new Intent(this, RouteMapActivity.class));
			}
		}
		// SETTINGS
		else if (v.getId() == R.id.button4) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		// Check Updates
		else if (v.getId() == R.id.button5) {
			// clear last time update
			Date version = new Date(System.currentTimeMillis());
			String path = new File(getCacheDir(), "transit_data.zip")
					.getAbsolutePath();
			new UpdateAsyncTask(version).execute(path);
		}
	}

	/* LoaderCallbacks<List<Route>> */

	@Override
	public Loader<List<Route>> onCreateLoader(int arg0, Bundle arg1) {
		CoreApplication app = (CoreApplication) getApplication();

		return app.m_loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Route>> arg0, List<Route> data) {
		CoreApplication app = (CoreApplication) getApplication();
		app.data = data;

		Intent intent = getIntent();
		intent.putExtra("download", false);

		if (m_runMap != null) {
			m_runMap.sendEmptyMessage(0);
			m_runMap = null;
		}

		m_loading.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<Route>> arg0) {
	}

	public static boolean isLoading = false;

	class UpdateAsyncTask extends AsyncTask<String, Integer, Boolean> {

		private Date m_version;

		public UpdateAsyncTask(Date version) {
			m_version = version;
		}

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(DashboardActivity.this);
			dialog.setMessage("Updating");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			boolean success = false;

			if (isLoading == false) {
				isLoading = true;
				String toCache = params[0];

				// loading from web
				ArrayList<com.bossly.lviv.transit.data.Route> routes = Main
						.LoadData(toCache);

				// save to db
				DatabaseSource db = new DatabaseSource(getApplicationContext());
				db.open();
				db.clear();

				for (com.bossly.lviv.transit.data.Route route : routes) {

					// ignore routes out of city
					if (route.insideCity(49.7422316, 23.8623047, 49.9529871,
							24.2056274)) {

						db.insertRoute(route.id, route.name, route.route,
								route.genDescription(), route.genPath());
					}
				}

				db.close();

				success = true;

				isLoading = false;
			}

			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			dialog.dismiss();

			// if download successfully-> save version
			if (result && m_version != null) {
				prefs.edit().putLong(PREF_LAST_UPDATE, m_version.getTime())
						.commit();

				// notify about data change
				Toast.makeText(getApplicationContext(), R.string.data_updated,
						Toast.LENGTH_SHORT).show();
			}

			// Prepare the loader. Either re-connect with an
			// existing one, or start a new one.
			getSupportLoaderManager().initLoader(0, null,
					DashboardActivity.this);
		}
	}

}
