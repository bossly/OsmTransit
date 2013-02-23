package com.bossly.lviv.transit.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.CircleOverlay;
import org.mapsforge.android.maps.overlay.OverlayCircle;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.overlay.OverlayWay;
import org.mapsforge.android.maps.overlay.WayOverlay;
import org.mapsforge.core.GeoPoint;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.bossly.lviv.transit.CoreApplication;
import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.R;
import com.bossly.lviv.transit.Route;
import com.bossly.lviv.transit.data.DatabaseSource;
import com.bossly.lviv.transit.data.RoutesContract;
import com.bossly.lviv.transit.data.RoutesContract.PointData;
import com.bossly.lviv.transit.data.RoutesContract.RouteData;
import com.bossly.lviv.transit.services.TrackingService;
import com.bossly.lviv.transit.utils.CommonUtils;

public class RouteMapActivity extends MapActivity implements OnClickListener, LocationListener,
		OnCheckedChangeListener
{
	public static final double MAX_DISTANCE = 300; // meters

	public static final String EXTRA_ROUTE = "extra_route";

	public static final String EXTRA_ROUTE_ID = "extra_route_id";

	private Button btnRoute;

	private Button btnPlus;

	private Button btnMinus;

	private ImageButton btnNotify;

	private RadioGroup m_routesGroup;

	private MapView mapView;

	private Location m_location = null;

	private LocationManager m_manager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);

		File cache = getCacheDir();

		File map_cache = new File(cache, "lviv1.map");

		if (!map_cache.exists())
		{
			// copy to cache
			try
			{
				InputStream in = getAssets().open("lviv.map");
				FileOutputStream fos = new FileOutputStream(map_cache, false);

				CommonUtils.copyData(in, fos);

				fos.close();
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		m_routesGroup = (RadioGroup) findViewById(R.id.routes);
		m_routesGroup.setOnCheckedChangeListener(this);

		mapView = (MapView) findViewById(R.id.mapView);
		btnRoute = (Button) findViewById(R.id.btnRoute);
		mapView.setClickable(true);
		// mapView.setMemoryCardCachePersistence( true );
		mapView.setBuiltInZoomControls(false);
		// mapView.setFpsCounter( true );
		// mapView.setZoomMin( ( byte )11 );
		mapView.getController().setZoom(13);
		// mapView.setZoomMax( ( byte )17 );

		// mapView.setMapViewMode( MapViewMode.CANVAS_RENDERER
		// );
		mapView.setMapFile(map_cache);

		// mapView.setOnClickListener( this );

		mapView.getOverlays().add(radius_geo);

		overlayRoutes = new RouteOverlay();
		mapView.getOverlays().add(overlayRoutes);

		Intent intent = getIntent();
		long routeId = intent.getLongExtra(EXTRA_ROUTE_ID, -1);

		if (routeId != -1)
		{
			showRoute(routeId);
		}
		else
		{
			mapView.getController().setCenter(new GeoPoint(49.839135448782514, 23.996164237976018));
		}

		da = getResources().getDrawable(R.drawable.ic_stat_a);
		da.setBounds(-20, -20, 20, 20);

		db = getResources().getDrawable(R.drawable.ic_stat_b);
		db.setBounds(-20, -20, 20, 20);

		dc = getResources().getDrawable(R.drawable.ic_stat_location);
		dc.setBounds(-15, -15, 15, 15);

		findViewById(R.id.btn_start).setOnClickListener(this);
		findViewById(R.id.btn_end).setOnClickListener(this);

		btnNotify = (ImageButton) findViewById(R.id.btnNotifyMe);
		btnNotify.setVisibility(View.INVISIBLE);
		btnNotify.setOnClickListener(this);

		btnMinus = (Button) findViewById(R.id.btn_minus);
		btnMinus.setOnClickListener(this);

		btnPlus = (Button) findViewById(R.id.btn_plus);
		btnPlus.setOnClickListener(this);

		findViewById(R.id.btn_current).setOnClickListener(this);

		m_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	Drawable da;

	Drawable db;

	Drawable dc;

	PPoint overlayPoints;

	PPoint start = null;

	PPoint end = null;

	PPoint current = null;

	RouteOverlay overlayRoutes;

	RadiusOverlay radius_geo = new RadiusOverlay();

	class RadiusOverlay extends CircleOverlay<OverlayCircle>
	{
		private OverlayCircle item;

		public RadiusOverlay()
		{
			super(null, null);

			Paint paintFill = new Paint();
			paintFill.setColor(Color.BLUE);
			paintFill.setStyle(Style.FILL);
			paintFill.setAlpha(100);

			Paint paintOutline = new Paint();
			paintOutline.setColor(Color.BLACK);
			paintOutline.setStyle(Style.STROKE);
			paintOutline.setStrokeWidth(1);
			paintOutline.setAlpha(150);

			item = new OverlayCircle(new GeoPoint(0, 0), 0, paintFill, paintOutline, "");
		}

		@Override
		public int size()
		{
			return 1;
		}

		@Override
		protected OverlayCircle createCircle(int i)
		{
			return item;
		}
	};

	@Override
	protected void onResume()
	{
		super.onResume();

		startDetermineUserLocation();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		stopDetermineUserLocation();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (nearTask != null)
		{
			nearTask.cancel(true);
			nearTask = null;
		}
	}

	static class RouteOverlay extends WayOverlay<OverlayWay>
	{
		OverlayWay item = null;

		static final Paint paintOutline = new Paint();

		public RouteOverlay()
		{
			super(null, paintOutline);

			paintOutline.setStrokeWidth(6);
			paintOutline.setStyle(Style.STROKE);
			paintOutline.setColor(Color.BLUE);
		}

		public void setPath(GeoPoint[][] wayNodes)
		{
			if (item != null)
			{
				item = null;

				Runtime r = Runtime.getRuntime();
				r.gc();
			}

			item = new OverlayWay(wayNodes);

			item.setPaint(null, paintOutline);

			this.requestRedraw();
		}

		@Override
		public int size()
		{
			return item != null ? 1 : 0;
		}

		@Override
		protected OverlayWay createWay(int i)
		{
			return item;
		}
	}

	private void setRoutePath(String sway)
	{
		if (sway == null || sway.length() < 2)
		{
			return;
		}

		GeoPoint[][] wayNodes = new GeoPoint[1][];

		String[] path = sway.substring(0, sway.length() - 1).split(";");
		GeoPoint[] points = new GeoPoint[path.length];
		Rect r = null;

		for (int j = 0; j < path.length; j++)
		{
			String[] coors = path[j].split(",");
			points[j] = new GeoPoint(Double.parseDouble(coors[0]), Double.parseDouble(coors[1]));

			int lat = points[j].latitudeE6;
			int lon = points[j].longitudeE6;

			if (r != null)
			{
				r.union(lat, lon);
			}
			else
			{
				r = new Rect(lat, lon, lat, lon);
			}
		}

		wayNodes[0] = points;

		if (r != null)
		{
			overlayRoutes.setPath(wayNodes);
		}
	}

	class PPoint extends ArrayItemizedOverlay
	{
		OverlayItem item;

		public PPoint(Drawable defaultMarker, GeoPoint point)
		{
			super(null);

			item = new OverlayItem(point, "title", "1", defaultMarker);
			addItem(item);
		}

		public GeoPoint getPoint()
		{
			return item.getPoint();
		}
	}

	class NearTasks extends AsyncTask<Object, Route, GeoPoint>
	{
		ArrayList<Route> route_to_add;

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			btnRoute.setText(R.string.lbl_searching);

			m_routesGroup.clearCheck();
			m_routesGroup.removeAllViews();

			overlayRoutes.item = null;
			overlayRoutes.requestRedraw();

			route_to_add = new ArrayList<Route>();
		}

		@Override
		protected void onPostExecute(GeoPoint result)
		{
			super.onPostExecute(result);

			if (!isCancelled())
			{
				btnRoute.setText("");

				// add routes to activity
				for (Route route : route_to_add)
				{
					RadioButton radio = (RadioButton) getLayoutInflater().inflate(R.layout.radio_route, null);

					radio.setText(route.toString());
					radio.setTag(route);

					RadioGroup.LayoutParams params = new LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
							RadioGroup.LayoutParams.WRAP_CONTENT);

					params.setMargins(5, 0, 5, 0);

					m_routesGroup.addView(radio, params);
				}

				RadioButton child = (RadioButton) m_routesGroup.getChildAt(0);

				if (child != null)
					m_routesGroup.check(child.getId());

			}
		}

		@Override
		protected GeoPoint doInBackground(Object... params)
		{
			// TODO
			// Cursor cursor = getContentResolver().query(PointData.CONTENT_URI, null, null, null, null);
			// ArrayList<String> startRoutes = new ArrayList<String>();
			// ArrayList<String> endRoutes = new ArrayList<String>();
			//
			// if(cursor.moveToFirst())
			// {
			// int latIndex = cursor.getColumnIndex(PointData.LATITUDE);
			// int lonIndex = cursor.getColumnIndex(PointData.LONGITUDE);
			// int idIndex = cursor.getColumnIndex(PointData.ROUTE_ID);
			//
			// do
			// {
			// double lon = cursor.getDouble(lonIndex);
			// double lat = cursor.getDouble(latIndex);
			//
			//
			// }
			// while(cursor.moveToNext());
			// }

			ArrayList<Route> data = CoreApplication.get(RouteMapActivity.this).data;
			
			if(data == null)
				return null;

			GeoPoint st = start.item.getPoint();
			GeoPoint st2 = end.item.getPoint();

			ArrayList<Route> f1 = GeoUtils.filterRoutes(data, st.getLatitude(), st.getLongitude(),
					MAX_DISTANCE);
			ArrayList<Route> f2 = GeoUtils.filterRoutes(f1, st2.getLatitude(), st2.getLongitude(),
					MAX_DISTANCE);

			for (Route route : f2)
			{
				this.publishProgress(route);
			}

			int lat1 = st.latitudeE6;
			int lon1 = st.longitudeE6;

			int lat2 = st2.latitudeE6;
			int lon2 = st2.longitudeE6;

			Rect r = new Rect(lat1, lon1, lat1, lon1);
			r.union(lat2, lon2, lat2, lon2);

			if (f2.size() > 0)
			{
				return new GeoPoint(r.centerX(), r.centerY());
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Route... values)
		{
			super.onProgressUpdate(values);

			route_to_add.add(values[0]);
		}
	}

	NearTasks nearTask;

	private Route current_route;

	private void findNear()
	{
		if (nearTask != null)
		{
			nearTask.cancel(true);
			nearTask = null;
		}

		nearTask = new NearTasks();
		nearTask.execute();
	}

	@Override
	public void onClick(View arg0)
	{
		if (arg0.getId() == R.id.btn_start)
		{
			if (start != null)
			{
				start.item.setPoint(getCenterPoint());
				start.requestRedraw();
			}
			else
			{
				start = new PPoint(da, getCenterPoint());
				// place point
				mapView.getOverlays().add(start);
			}

			// find near routes
			if (start != null && end != null)
			{
				findNear();
			}
		}
		else if (arg0.getId() == R.id.btn_end)
		{
			if (end != null)
			{
				end.item.setPoint(getCenterPoint());
				end.requestRedraw();
			}
			else
			{
				end = new PPoint(db, getCenterPoint());
				// place point
				mapView.getOverlays().add(end);
			}

			btnNotify.setVisibility(View.VISIBLE);

			// find near routes
			if (start != null && end != null)
			{
				findNear();
			}
		}
		else if (arg0.getId() == R.id.btn_current)
		{
			if (current != null)
			{
				mapView.getController().setCenter(current.getPoint());
			}
		}
		else if (arg0.getId() == R.id.btn_plus)
		{
			mapView.getController().zoomIn();
		}
		else if (arg0.getId() == R.id.btn_minus)
		{
			mapView.getController().zoomOut();
		}
		else if (arg0.getId() == R.id.btnNotifyMe)
		{
			if (end != null)
			{
				Intent in = new Intent(this, TrackingService.class);
				GeoPoint point = end.item.getPoint();

				in.putExtra("lat", (double) point.getLatitude());
				in.putExtra("lon", (double) point.getLongitude());

				startService(in);
			}
		}
	}

	/* Search near routes */

	private GeoPoint getCenterPoint()
	{
		int x = mapView.getWidth() / 2;
		int y = mapView.getHeight() / 2;
		GeoPoint point = mapView.getProjection().fromPixels((int) x, (int) y);

		return point;
	}

	public void startDetermineUserLocation()
	{
		if (m_manager != null)
		{
			if (m_manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				m_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, this);

				Location location = m_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				onLocationUpdated(location);
			}
			else
			{
				// notify about GPS if off
				/*
				 * Builder builder = new Builder( this ); builder.setTitle( getString(
				 * R.string.dlg_location_title ) ); builder.setMessage( R.string.dlg_location_message );
				 * 
				 * builder.setPositiveButton( R.string.dlg_settings, new DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick( DialogInterface dialog, int which ) {
				 * startActivityForResult( new Intent(
				 * android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS ), 0 ); } } );
				 * 
				 * builder.setNegativeButton( android.R.string.cancel, null );
				 * 
				 * builder.show();
				 */
			}

			if (m_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			{
				m_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 100, this);

				Location location = m_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

				onLocationUpdated(location);
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

	private void onLocationUpdated(Location location)
	{
		if (location != null && GeoUtils.isBetterLocation(location, m_location, TWO_MINUTES))
		{
			m_location = location;

			GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

			if (radius_geo != null)
			{
				radius_geo.item.setCircleData(point, location.getAccuracy());
			}

			if (current != null)
			{
				current.item.setPoint(point);
				current.requestRedraw();
			}
			else
			{
				current = new PPoint(dc, point);
				// place point
				mapView.getOverlays().add(current);
			}

			if (start == null)
			{
				start = new PPoint(da, new GeoPoint(location.getLatitude(), location.getLongitude()));

				// place point
				mapView.getOverlays().add(start);
			}
		}
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/* LocationListener */

	@Override
	public void onLocationChanged(Location location)
	{
		onLocationUpdated(location);
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

	/* OnCheckedChangeListener */

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		if (checkedId > 0)
		{
			RadioButton btn = (RadioButton) group.findViewById(checkedId);
			Route route = (Route) btn.getTag();

			showRoute( route );
		}
	}

	private void showRoute(Route route)
	{
		setRoutePath(route.path);
		current_route = route;

		btnRoute.setText(route.toString());
	}

	private void showRoute(long id)
	{
		Uri routeUri = ContentUris.withAppendedId(RouteData.CONTENT_URI, id);
		Cursor cursor = getContentResolver().query(routeUri, new String[] { RouteData.PATH }, null,
				null, null);

		if (cursor.moveToFirst())
		{
			setRoutePath(cursor.getString(0));
		}

		cursor.close();
	}
}
