package com.bossly.lviv.transit.fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.RouteCursorAdapter;
import com.bossly.lviv.transit.activities.GeoLocationBaseActivity;
import com.bossly.lviv.transit.data.RoutesContract;
import com.bossly.lviv.transit.data.RoutesContract.RouteData;
import com.bossly.lviv.transit.utils.CommonUtils;

public class NearRoutesFragment extends Fragment implements
		LoaderCallbacks<Cursor>, OnItemClickListener,
		android.widget.RadioGroup.OnCheckedChangeListener {

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private static final double BOUNDS_SIZE = 0.003;

	public ListView vListView;

	private int selected = 0;

	private int position = 0;

	private int scroll_y;

	private TextView m_textStatus;

	private Location m_location = null;

	private RouteCursorAdapter mCursorAdapter;

	private String mCursorFilter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View content = inflater.inflate(R.layout.fr_near_routes, container,
				false);

		vListView = (ListView) content.findViewById(android.R.id.list);
		RadioGroup radio = (RadioGroup) content
				.findViewById(R.id.v_rdgp_routes);
		radio.setOnCheckedChangeListener(this);

		vListView.setEmptyView(content.findViewById(android.R.id.empty));
		vListView.setOnItemClickListener(this);

		m_textStatus = (TextView) content.findViewById(R.id.v_status);

		if (savedInstanceState != null) {
			scroll_y = savedInstanceState.getInt("scroll_y");
			position = savedInstanceState.getInt("position");
			selected = savedInstanceState.getInt("selected");
		}

		return content;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		// save scroll position
		if (vListView != null && vListView.getChildCount() > 0) {
			outState.putInt("scroll_y", vListView.getChildAt(0).getTop());
			outState.putInt("position", vListView.getFirstVisiblePosition());
		}

		outState.putInt("selected", selected);
	}

	/* TextWatcher */

	public void setFilter(String query) {
		mCursorFilter = query;
		getLoaderManager().restartLoader(0, null, this);

		if (mCursorAdapter == null)
			return;

		if (TextUtils.isEmpty(mCursorFilter)) {
			mCursorAdapter.setFilterHighlight(null);
		} else {
			mCursorAdapter.setFilterHighlight(mCursorFilter.split(" "));
		}
	}

	/* OnItemClickListener */

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		Uri routeUri = ContentUris.withAppendedId(RouteData.CONTENT_URI, id);
		Cursor cursor = getActivity().getContentResolver().query(routeUri,
				new String[] { RouteData.PATH }, null, null, null);

		if (cursor.moveToFirst()) {
			String sway = cursor.getString(0);

			if (sway == null || sway.length() < 2) {
				return;
			}

			CommonUtils.displayMap(getActivity(), sway);
		}

		cursor.close();
	}

	/* Search near routes */
	public boolean onLocationUpdated(Location location) {

		boolean success = false;

		if (location != null && location.getAccuracy() < 20) // if less than 20
																// meters
		{
			// save user power. stop determine location
			GeoLocationBaseActivity activity = (GeoLocationBaseActivity) getActivity();
			activity.stopDetermineUserLocation();
		}

		if (location != null
				&& CommonUtils.isBetterLocation(location, m_location, TWO_MINUTES)) {

			success = true;

			m_location = location;

			if (m_textStatus != null) {

				float accuracy = location.getAccuracy();

				if (accuracy > 700) {
					m_textStatus.setBackgroundColor(Color.RED);
				} else if (accuracy > 400) {
					m_textStatus.setBackgroundColor(Color.YELLOW);
				} else {
					m_textStatus.setBackgroundColor(Color
							.argb(255, 10, 200, 10));
				}

				m_textStatus.setText(String.format("Точність: < %.1f метрів",
						accuracy));
			}

			getLoaderManager().restartLoader(0, null, this);

			if (vListView != null) {
				vListView.setSelectionFromTop(position, scroll_y);
			}
		}

		return success;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		GeoLocationBaseActivity activity = (GeoLocationBaseActivity) getActivity();

		switch (checkedId) {
		case R.id.v_rd_near:
			activity.startDetermineUserLocation();
			m_textStatus.setVisibility(View.VISIBLE);
			break;

		case R.id.v_rd_default:
			activity.stopDetermineUserLocation();
			m_location = null;
			getLoaderManager().restartLoader(0, null, this);
			m_textStatus.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String selection = null;
		Uri contentUri = RoutesContract.RouteData.CONTENT_URI;

		if (!TextUtils.isEmpty(mCursorFilter)) {
			String[] filters = mCursorFilter.split(" ");
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append(RoutesContract.RouteData.SEARCH);
			stringBuilder.append(" LIKE '%");

			for (int i = 0; i < filters.length; i++) {
				if (i != 0)
					stringBuilder.append("%");

				stringBuilder.append(filters[i]);
			}

			stringBuilder.append("%'");

			selection = stringBuilder.toString();
		}

		if (m_location != null) {
			StringBuilder boundsBuilder = new StringBuilder();

			boundsBuilder.append(m_location.getLatitude() - BOUNDS_SIZE);
			boundsBuilder.append(";");
			boundsBuilder.append(m_location.getLatitude() + BOUNDS_SIZE);
			boundsBuilder.append(";");
			boundsBuilder.append(m_location.getLongitude() - BOUNDS_SIZE);
			boundsBuilder.append(";");
			boundsBuilder.append(m_location.getLongitude() + BOUNDS_SIZE);

			contentUri = Uri.withAppendedPath(
					RoutesContract.RouteData.CONTENT_BOUNDS_URI,
					boundsBuilder.toString());
		}

		return new CursorLoader(getActivity(), contentUri, null, selection,
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor result) {
		if (mCursorAdapter == null) {
			mCursorAdapter = new RouteCursorAdapter(getActivity(), result,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			vListView.setAdapter(mCursorAdapter);
		} else {
			mCursorAdapter.swapCursor(result);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
