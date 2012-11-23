package com.bossly.lviv.transit.activities;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.bossly.lviv.transit.CoreApplication;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.services.TransitService;

public class DashboardActivity extends android.support.v4.app.FragmentActivity
    implements OnClickListener, LoaderCallbacks<List<Route>>
{

	public static final String PREF_LAST_UPDATE = "pref_last_date";

	private Handler m_runMap = null;

	private View m_loading;

	private BroadcastReceiver mServiceReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO
			getSupportLoaderManager().getLoader(0).startLoading();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dashboard);

		m_loading = findViewById(R.id.loading);

		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.button4).setOnClickListener(this);
		findViewById(R.id.v_btn_update).setOnClickListener(this);

		getSupportLoaderManager().initLoader(0, null, this);

		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
		    new IntentFilter(TransitService.ACTION_UPDATED));
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// start self-working service
		// startService(new Intent(this, TransitService.class));
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		getSupportLoaderManager().destroyLoader(0);
		LocalBroadcastManager.getInstance(this)
		    .unregisterReceiver(mServiceReceiver);
	}

	@Override
	public void onClick(View v)
	{
		CoreApplication app = (CoreApplication) getApplication();

		// NEAR
		if (v.getId() == R.id.button1)
		{
			final Intent i = new Intent(this, NearRoutesActivity.class);

			if (app.data == null)
			{
				m_runMap = new Handler()
				{
					public void handleMessage(android.os.Message msg)
					{
						startActivity(i);
					};
				};
			} else
			{
				startActivity(i);
			}
		}
		// MAP
		else if (v.getId() == R.id.button3)
		{
			if (app.data == null)
			{
				m_runMap = new Handler()
				{
					public void handleMessage(android.os.Message msg)
					{
						startActivity(new Intent(DashboardActivity.this,
						    RouteMapActivity.class));
					};
				};
			} else
			{
				startActivity(new Intent(this, RouteMapActivity.class));
			}
		}
		// SETTINGS
		else if (v.getId() == R.id.button4)
		{
			startActivity(new Intent(this, SettingsActivity.class));
		}
		// Check Updates
		else if (v.getId() == R.id.v_btn_update)
		{
			// clear last time update
			// Date version = new Date(System.currentTimeMillis());
			// String path = new File(getCacheDir(), "transit_data.zip")
			// .getAbsolutePath();
			// new UpdateAsyncTask(version).execute(path);

			startService(new Intent(this, TransitService.class));
		}
	}

	/* LoaderCallbacks<List<Route>> */
	@Override
	public Loader<List<Route>> onCreateLoader(int arg0, Bundle arg1)
	{
		CoreApplication app = (CoreApplication) getApplication();

		return app.m_loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Route>> arg0, List<Route> data)
	{
		CoreApplication app = (CoreApplication) getApplication();
		app.data = data;

		if (m_runMap != null)
		{
			m_runMap.sendEmptyMessage(0);
			m_runMap = null;
		}

		m_loading.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<Route>> arg0)
	{
	}
}
