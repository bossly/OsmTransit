package com.bossly.lviv.transit.fragments;

import java.util.ArrayList;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bossly.lviv.transit.CoreApplication;
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

	TextView txtStart;
	TextView txtDestination;
	Address adrDestination;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.map, null);

		txtStart = (TextView) getView().findViewById(R.id.textFrom);
		txtDestination = (TextView) getView().findViewById(R.id.textTo);

		if (adrDestination != null)
			txtDestination.setText("до: " + fromAddress(adrDestination));

		return view;
	}

	public void setDestination(Address r) {

		m_locationTo = new Location("Unknown");
		m_locationTo.setLatitude(r.getLatitude());
		m_locationTo.setLongitude(r.getLongitude());

		adrDestination = r;

		if (txtDestination != null) {
			txtDestination.setText("до: " + fromAddress(r));
		}
	}

	private void findRoutesFromTo() {

		if (m_location != null && m_locationTo != null) {

			ArrayList<Route> data = CoreApplication.get(getActivity()).data;

			if (data == null) {
				DatabaseSource db = new DatabaseSource(getActivity());
				db.open();
				data = db.getRoutes();
				db.close();
			}

			if (data == null)
				return;

			ArrayList<Route> f1 = GeoUtils.filterRoutes(data,
					m_location.getLatitude(), m_location.getLongitude(),
					MAX_DISTANCE);
			ArrayList<Route> f2 = GeoUtils.filterRoutes(f1,
					m_locationTo.getLatitude(), m_locationTo.getLongitude(),
					MAX_DISTANCE);

			Log.d("Routes founded:", "" + f2.size());

			RouteAdapter adapter = new RouteAdapter(getActivity(), f2);
			setListAdapter(adapter);
		}
	}

	private String fromAddress(Address adr) {
		StringBuilder b = new StringBuilder();

		for (int i = 0; i < 10; i++) {
			String line = adr.getAddressLine(i);

			if (line == null)
				break;

			b.append(line + ", ");
		}

		return b.toString();
	}

	public void onLocationUpdated(Location location) {

		if (location != null
				&& GeoUtils.isBetterLocation(location, m_location, TWO_MINUTES)) {

			// if (coder.isPresent())
			{
				// try {
				// List<Address> result = coder.getFromLocation(
				// location.getLatitude(), location.getLongitude(), 1);
				//
				// if (result.size() > 0) {
				// Address r = result.get(0);
				//
				// if (txtStart != null) {
				// txtStart.setText("від: " + fromAddress(r));
				// }
				// }
				//
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}

			m_location = location;

			findRoutesFromTo();
		}
	}
}
