package com.bossly.lviv.transit.activities;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SearchViewCompat;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.fragments.NearRoutesFragment;
import com.bossly.lviv.transit.services.TransitService;

public class DashboardActivity extends SherlockFragmentActivity {

	public static final String PREF_LAST_UPDATE = "pref_last_date";
	final NearRoutesFragment nearFragment = new NearRoutesFragment();

	private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);

		if (savedInstanceState == null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.add(android.R.id.content, nearFragment);
			transaction.commit();
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mServiceReceiver,
				new IntentFilter(TransitService.ACTION_UPDATED));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

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
			android.widget.SearchView search = (android.widget.SearchView) searchView;
			// Use native implementation
			search.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextChange(String newText) {
					// Do something
					// System.out.println("onQueryTextChange----------");
					nearFragment.setFilter(newText);
					return true;
				}

				@Override
				public boolean onQueryTextSubmit(String query) {
					// Do something
					System.out.println("onQueryTextSubmit----------");
					return true;
				}
			});

		} else {
			// Use simple compatibility implementation
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
}
