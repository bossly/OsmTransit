package com.bossly.lviv.transit.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SearchViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bossly.lviv.transit.CoreApplication;
import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.RouteAdapter;
import com.bossly.lviv.transit.RouteCursorAdapter;
import com.bossly.lviv.transit.RoutesLoader;
import com.bossly.lviv.transit.activities.RouteMapActivity;
import com.bossly.lviv.transit.data.RoutesContract;

public class NearRoutesFragment extends Fragment implements LoaderCallbacks<Cursor>, TextWatcher,
		OnClickListener, OnItemClickListener, LocationListener,
		android.widget.RadioGroup.OnCheckedChangeListener
{

	public EditText vEditText;

	public ListView vListView;

	public RouteAdapter m_adapter;

	private int selected = 0;

	private int position = 0;

	private int scroll_y;

	private TextView m_textStatus;

	private ArrayList<Route> m_data = null;

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
		
		vEditText = (EditText) content.findViewById(R.id.editText1);
		vListView = (ListView) content.findViewById(R.id.listView1);
		RadioGroup radio = (RadioGroup) content.findViewById(R.id.radioGroup1);
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

		m_textStatus = (TextView) content.findViewById(R.id.textStatus);

		content.findViewById(R.id.button_reload).setOnClickListener(this);

		if (savedInstanceState != null)
		{
			scroll_y = savedInstanceState.getInt("scroll_y");
			position = savedInstanceState.getInt("position");
			selected = savedInstanceState.getInt("selected");
		}

		CoreApplication app = (CoreApplication) getActivity().getApplication();

		m_data = new ArrayList<Route>(app.data);
		m_adapter = new RouteAdapter(getActivity(), m_data);
		vListView.setAdapter(m_adapter);

	
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

	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.obj != null)
			{
				if (m_adapter != null)
				{
					m_adapter.getFilter().filter(msg.obj.toString());
				}
			}
		};
	};

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		mCursorFilter = s.toString();
		getLoaderManager().restartLoader(0, null, this);
		
		if(TextUtils.isEmpty(mCursorFilter))
		{
			mCursorAdapter.setFilterHighlight(null);
		}
		else
		{
			mCursorAdapter.setFilterHighlight(mCursorFilter.split(" "));
		}
	}

	@Override
	public void onClick(View v)
	{
		vEditText.setText(new String());
	}

	/* OnItemClickListener */

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		hideKeyboard();

		scroll_y = vListView.getChildAt(0).getTop();
		position = vListView.getFirstVisiblePosition();
		selected = arg2;

		Route route = (Route) arg0.getItemAtPosition(arg2);

		Intent intent = new Intent(getActivity(), RouteMapActivity.class);
		intent.putExtra(RouteMapActivity.EXTRA_ROUTE, route);
		startActivity(intent);
	}

	/* Search near routes */

	public void startDetermineUserLocation()
	{
		if (m_manager != null)
		{
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

			m_adapter.updateByLocation(m_location);

			if (vListView != null)
			{
				vListView.setSelectionFromTop(position, scroll_y);
			}
		}

		return success;
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

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

		Location loc;

		if (checkedId == R.id.radio0)
		{
			loc = null;
			stopDetermineUserLocation();
		}
		else
		{
			loc = m_location; // last known location
			startDetermineUserLocation();
		}

		// update list
		m_adapter.updateByLocation(loc);

		if (vEditText != null)
		{
			m_adapter.getFilter().filter(vEditText.getText().toString());
		}

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		String selection = null;
		
		if(!TextUtils.isEmpty(mCursorFilter))
		{
			selection = String.format("upper(%s) LIKE upper('%%%s%%')", RoutesContract.RouteData.DIRECTION, mCursorFilter);
		}
		
		return new CursorLoader(getActivity(), RoutesContract.RouteData.CONTENT_URI, null, selection, null, null);
	}


	@Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor result)
  {
		if(mCursorAdapter == null)
		{
			mCursorAdapter = new RouteCursorAdapter(getActivity(), result, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
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
