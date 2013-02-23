package com.bossly.lviv.transit.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.services.TransitService;

public class DashboardActivity extends FragmentActivity implements OnClickListener
{

	public static final String PREF_LAST_UPDATE = "pref_last_date";

	private BroadcastReceiver mServiceReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dashboard);

		findViewById(R.id.v_btn_routes).setOnClickListener(this);
		findViewById(R.id.v_btn_map).setOnClickListener(this);
		findViewById(R.id.v_btn_prefs).setOnClickListener(this);
		findViewById(R.id.v_btn_update).setOnClickListener(this);

		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
				new IntentFilter(TransitService.ACTION_UPDATED));
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiver);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.v_btn_routes:
				startActivity(new Intent(this, NearRoutesActivity.class));
				break;

			case R.id.v_btn_map:
				startActivity(new Intent(this, RouteMapActivity.class));
				break;

			case R.id.v_btn_prefs:
				startActivity(new Intent(this, SettingsActivity.class));
				break;

			case R.id.v_btn_update:
				startService(new Intent(this, TransitService.class));
				break;
		}
	}
}
