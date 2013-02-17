package com.bossly.lviv.transit.fragments;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.RouteCursorAdapter;
import com.bossly.lviv.transit.activities.RouteMapActivity;
import com.bossly.lviv.transit.data.RoutesContract;

public class NearRoutesFragment extends Fragment implements LoaderCallbacks<Cursor>, TextWatcher,
		OnClickListener, OnItemClickListener, LocationListener,
		android.widget.RadioGroup.OnCheckedChangeListener
{
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private static final double BOUNDS_SIZE = 0.003; 

	public EditText vEditText;

	public ListView vListView;

	private int selected = 0;

	private int position = 0;

	private int scroll_y;

	private TextView m_textStatus;

	private Location m_location = null;

	private LocationManager m_manager;

	private RouteCursorAdapter mCursorAdapter;

	private String mCursorFilter;

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		m_manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		stopDetermineUserLocation();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View content = inflater.inflate(R.layout.fr_near_routes, container, false);

		vEditText = (EditText) content.findViewById(R.id.v_search);
		vListView = (ListView) content.findViewById(android.R.id.list);
		RadioGroup radio = (RadioGroup) content.findViewById(R.id.v_rdgp_routes);
		radio.setOnCheckedChangeListener(this);

		vEditText.addTextChangedListener(this);
		vEditText.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_CALL)
				{
					hideKeyboard();

					return true;
				}

				return false;
			}
		});

		vListView.setEmptyView(content.findViewById(android.R.id.empty));
		vListView.setOnItemClickListener(this);

		m_textStatus = (TextView) content.findViewById(R.id.v_status);

		content.findViewById(R.id.v_clear).setOnClickListener(this);

		if (savedInstanceState != null)
		{
			scroll_y = savedInstanceState.getInt("scroll_y");
			position = savedInstanceState.getInt("position");
			selected = savedInstanceState.getInt("selected");
		}

		return content;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{

		super.onSaveInstanceState(outState);

		// save scroll position
		if (vListView != null && vListView.getChildCount() > 0)
		{
			outState.putInt("scroll_y", vListView.getChildAt(0).getTop());
			outState.putInt("position", vListView.getFirstVisiblePosition());
		}

		outState.putInt("selected", selected);
	}

	private void hideKeyboard()
	{
		// hide virtual keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		imm.hideSoftInputFromWindow(vEditText.getWindowToken(), 0);
	}

	/* TextWatcher */

	@Override
	public void afterTextChanged(Editable e)
	{
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		mCursorFilter = s.toString();
		getLoaderManager().restartLoader(0, null, this);

		if (TextUtils.isEmpty(mCursorFilter))
		{
			mCursorAdapter.setFilterHighlight(null);
		}
		else
		{
			mCursorAdapter.setFilterHighlight(mCursorFilter.split(" "));
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onClick(View v)
	{
		vEditText.setText(new String());
	}

	/* OnItemClickListener */

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		hideKeyboard();

		Intent intent = new Intent(getActivity(), RouteMapActivity.class);
		intent.putExtra(RouteMapActivity.EXTRA_ROUTE_ID, id);
		startActivity(intent);
	}

	/* Search near routes */

	public void startDetermineUserLocation()
	{
		if (m_manager != null)
		{
			m_textStatus.setVisibility(View.VISIBLE);
			
			if (m_manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				Location location = m_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				onLocationUpdated(location);

				m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 0, this);
			}
			else
			{
				// notify about GPS if off
				Builder builder = new Builder(getActivity());
				builder.setTitle(getString(R.string.dlg_location_title));
				builder.setMessage(R.string.dlg_location_message);

				builder.setPositiveButton(R.string.dlg_settings, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						startActivityForResult(new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
					}
				});

				builder.setNegativeButton(android.R.string.cancel, null);

				builder.show();
			}

			if (m_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			{
				Location location = m_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				onLocationUpdated(location);

				m_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 0, this);
			}
			else
			{
				// notify user about network location feature
			}
		}
	}

	public void stopDetermineUserLocation()
	{
		if (m_manager != null)
		{
			m_location = null;
			getLoaderManager().restartLoader(0, null, this);
			m_textStatus.setVisibility(View.GONE);
			m_manager.removeUpdates(this);
		}
	}

	private boolean onLocationUpdated(Location location)
	{
		boolean success = false;

		if (location != null && GeoUtils.isBetterLocation(location, m_location, TWO_MINUTES))
		{

			success = true;

			m_location = location;

			if (m_textStatus != null)
			{

				float accuracy = location.getAccuracy();

				if (accuracy > 700)
				{
					m_textStatus.setBackgroundColor(Color.RED);
				}
				else if (accuracy > 400)
				{
					m_textStatus.setBackgroundColor(Color.YELLOW);
				}
				else
				{
					m_textStatus.setBackgroundColor(Color.argb(255, 10, 200, 10));
				}

				m_textStatus.setText(String.format("Точність: < %.1f метрів", accuracy));
			}

			getLoaderManager().restartLoader(0, null, this);
			
			if (vListView != null)
			{
				vListView.setSelectionFromTop(position, scroll_y);
			}
		}

		return success;
	}

	/* LocationListener */

	@Override
	public void onLocationChanged(Location location)
	{
		if (onLocationUpdated(location))
		{

			if (location.getAccuracy() < 20) // if less than 20 meters
			{
				// save user power. stop determine location
				stopDetermineUserLocation();
			}

		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		switch (checkedId)
		{
			case R.id.v_rd_near:
				startDetermineUserLocation();
				break;

			case R.id.v_rd_default:
				stopDetermineUserLocation();
				break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		String selection = null;
		Uri contentUri = RoutesContract.RouteData.CONTENT_URI;

		if (!TextUtils.isEmpty(mCursorFilter))
		{
			String[] filters = mCursorFilter.split(" ");
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append(RoutesContract.RouteData.SEARCH);
			stringBuilder.append(" LIKE '%");

			for (int i = 0; i < filters.length; i++)
			{
				if (i != 0)
					stringBuilder.append("%");

				stringBuilder.append(filters[i]);
			}

			stringBuilder.append("%'");
			
			selection = stringBuilder.toString();
		}
		
		if(m_location != null)
		{
			StringBuilder boundsBuilder = new StringBuilder();

			boundsBuilder.append(m_location.getLatitude() - BOUNDS_SIZE);
			boundsBuilder.append(";");
			boundsBuilder.append(m_location.getLatitude() + BOUNDS_SIZE);
			boundsBuilder.append(";");
			boundsBuilder.append(m_location.getLongitude() - BOUNDS_SIZE);
			boundsBuilder.append(";");
			boundsBuilder.append(m_location.getLongitude() + BOUNDS_SIZE);
			
			contentUri = Uri.withAppendedPath(RoutesContract.RouteData.CONTENT_BOUNDS_URI, boundsBuilder.toString());
		}

		return new CursorLoader(getActivity(), contentUri, null, selection,
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor result)
	{
		if (mCursorAdapter == null)
		{
			mCursorAdapter = new RouteCursorAdapter(getActivity(), result,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			vListView.setAdapter(mCursorAdapter);
		}
		else
		{
			mCursorAdapter.swapCursor(result);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
	}
}
