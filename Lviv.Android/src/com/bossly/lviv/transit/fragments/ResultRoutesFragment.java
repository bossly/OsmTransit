package com.bossly.lviv.transit.fragments;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.RouteAdapter;
import com.bossly.lviv.transit.data.DatabaseSource;

public class ResultRoutesFragment extends ListFragment {

	private static final double MAX_DISTANCE = 300; // meters
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private Location m_location = null;
	private Location m_locationTo = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fr_routes, null);

		return view;
	}

	public void setDestination(Address r) {
		m_locationTo = new Location("Unknown");
		m_locationTo.setLatitude(r.getLatitude());
		m_locationTo.setLongitude(r.getLongitude());
	}

	public void onLocationUpdated(Location location) {

		if (location != null
				&& GeoUtils.isBetterLocation(location, m_location, TWO_MINUTES)) {

			m_location = location;

			if (task != null) {
				task.cancel(true);
				task = null;
			}

			task = new RouteAsyncTask();
			task.execute(m_location, m_locationTo);
		}
	}

	RouteAsyncTask task;

	class RouteAsyncTask extends AsyncTask<Location, Void, ArrayList<Route>> {

		ProgressDialog dialog;

		@Override
		protected ArrayList<Route> doInBackground(Location... params) {

			ArrayList<Route> adapter = null;
			Location from = params[0];
			Location to = params[0];

			if (from != null && to != null) {

				ArrayList<Route> data = null;
				DatabaseSource db = new DatabaseSource(getActivity());
				db.open();
				data = db.getRoutes();
				db.close();

				if (data != null) {

					ArrayList<Route> f1 = GeoUtils.filterRoutes(data,
							m_location.getLatitude(),
							m_location.getLongitude(), MAX_DISTANCE);

					ArrayList<Route> f2 = GeoUtils.filterRoutes(f1,
							m_locationTo.getLatitude(),
							m_locationTo.getLongitude(), MAX_DISTANCE);

					Log.d("Routes founded:", "" + f2.size());
					adapter = f2;
				}
			}

			return adapter;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(getActivity());
			dialog.setMessage("Searching for route...");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected void onPostExecute(ArrayList<Route> adapter) {

			dialog.dismiss();
			setListAdapter(new RouteAdapter(getActivity(), adapter));

			super.onPostExecute(adapter);
		}
	}
}
