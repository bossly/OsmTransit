package com.bossly.lviv.transit.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.fragments.NearRoutesFragment;
import com.bossly.lviv.transit.services.TransitService;

public class RoutesActivity extends GeoLocationBaseActivity {

	private NearRoutesFragment nearFragment;

	private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final FragmentManager fragmentManager = getSupportFragmentManager();

		if (savedInstanceState == null) {
			nearFragment = new NearRoutesFragment();
			
			FragmentTransaction transaction = fragmentManager
					.beginTransaction();
			transaction.add(android.R.id.content, nearFragment,
					NearRoutesFragment.class.getName());
			transaction.commit();
		} else {
			nearFragment = (NearRoutesFragment) fragmentManager
					.findFragmentByTag(NearRoutesFragment.class.getName());
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mServiceReceiver,
				new IntentFilter(TransitService.ACTION_UPDATED));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopDetermineUserLocation();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mServiceReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getSupportMenuInflater();

		// collapse search
		Context context = getSupportActionBar().getThemedContext();
		View searchView = SearchViewCompat.newSearchView(context);

		if (searchView != null) {

			SearchViewCompat.setOnQueryTextListener(searchView,
					new OnQueryTextListenerCompat() {
						@Override
						public boolean onQueryTextChange(String newText) {
							nearFragment.setFilter(newText);
							return true;
						}

						@Override
						public boolean onQueryTextSubmit(String query) {
							return true;
						}
					});
		}

		MenuItem searchItem = menu.add("search").setIcon(
				R.drawable.ic_action_search);
		searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		searchItem.setActionView(searchView);
		searchItem.expandActionView();

		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.itemUpdate:
			startService(new Intent(this, TransitService.class));
			break;

		case R.id.itemPref:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}

		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onLocationUpdated(Location location) {

		if (nearFragment != null) {
			nearFragment.onLocationUpdated(location);
		}
	}

}
