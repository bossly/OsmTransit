package com.bossly.lviv.transit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bossly.lviv.transit.GeoUtils.Point2D;

public class RouteAdapter extends BaseAdapter implements Filterable {

	public static final double MAX_DISTANCE = 300; // meters

	ArrayList<Route> items = null;
	ArrayList<Route> filtered = null;

	public Location locationToSort = null;

	final int resource = R.layout.item_route;

	public RouteAdapter(Context context, List<Route> objects) {
		super();

		items = new ArrayList<Route>(objects);
	}

	@Override
	public int getCount() {
		if (filtered != null) {
			return filtered.size();
		} else {
			return getInternalCount();
		}
	}

	private int getInternalCount() {
		return items.size();
	}

	private Route getInternalItem(int position) {
		return items.get(position);
	}

	@Override
	public Route getItem(int position) {
		if (filtered != null) {
			return filtered.get(position);
		} else {
			return getInternalItem(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					resource, null);
		}

		TextView text1 = (TextView) convertView
				.findViewById(android.R.id.text1);
		TextView text2 = (TextView) convertView
				.findViewById(android.R.id.text2);
		TextView text3 = (TextView) convertView.findViewById(R.id.text3);

		Route route = getItem(position);

		text1.setText(route.toString());

		text2.setText("");
		text2.append(route.desc.replace(" вулиця", ""));

		if (cur_filter_text != null && cur_filter_text.length() > 0) {
			String[] filters = cur_filter_text.toString().toLowerCase()
					.split(" ");
			int filters_count = filters.length;

			Spannable sText = (Spannable) text2.getText();

			String ds = route.desc.toLowerCase();

			for (int i = 0; i < filters_count; i++) {
				int pos = ds.indexOf(filters[i]);

				while (pos >= 0) {
					sText.setSpan(new BackgroundColorSpan(Color.YELLOW), pos,
							pos + filters[i].length(), 0);

					pos = ds.indexOf(filters[i], pos + 1);
				}
			}
		}

		text3.setText("");

		return convertView;
	}

	RouteFilter filter = new RouteFilter();

	@Override
	public Filter getFilter() {
		return filter;
	}

	private CharSequence cur_filter_text = null;

	private class RouteFilter extends Filter {

		private boolean isGood(String constraint, Route route) {

			String name = route.name.toLowerCase();
			String desc = route.desc.toLowerCase();

			String[] words = constraint.split(" ");
			int length = words.length;
			int prev = 0;

			boolean success = false;

			// find direction
			if (length > 1) {
				success = true;

				for (int i = 0; i < length; i++) {
					String seq = words[i].trim().toLowerCase();
					int cur = desc.indexOf(seq);

					if (cur < prev) {
						success = false;
						break;
					}

					prev = cur;
				}
			}
			// find route
			else {
				String seq = words[0].toLowerCase();

				if (name.contains(seq) || desc.contains(seq)) {
					success = true;
				}
			}

			return success;
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			// NOTE: this function is *always* called from a
			// background thread, and not the UI thread.
			String text = constraint.toString().toLowerCase();

			FilterResults result = new FilterResults();

			if (text != null && text.length() > 0) {
				ArrayList<Route> items = new ArrayList<Route>();
				ArrayList<Route> local = new ArrayList<Route>();

				synchronized (this) {
					int count = getInternalCount();

					for (int i = 0; i < count; i++) {
						local.add(getInternalItem(i));
					}
				}

				for (Route r : local) {
					// if good -> add
					if (isGood(text, r)) {
						items.add(r);
					}
				}

				result.count = items.size();
				result.values = items;
			} else {
				synchronized (this) {
					result.values = null;
					result.count = 0;
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {

			cur_filter_text = constraint;

			// NOTE: this function is *always* called from the
			// UI thread.
			if (results.values == null) {
				filtered = null;
			} else {
				filtered = (ArrayList<Route>) results.values;
				Collections.sort(filtered);
			}

			notifyDataSetChanged();
		}

	}

	public void updateByLocation(Location m_location) {

		locationToSort = m_location;

		// update distance
		int routes_count = items.size();

		for (int i = 0; i < routes_count; i++) {

			Route route = items.get(i);
			route.min_distance = Integer.MAX_VALUE;

			if (m_location == null)
				continue;

			// calc short way to route
			if (route.path != null && route.path.length() > 0) {

				String[] points = route.path.split(";");
				route.points = points.length;

				for (int j = 0, count = points.length; j < count; j++) {

					String[] coord = points[j].split(",");

					double clat = Double.parseDouble(coord[0]);
					double clng = Double.parseDouble(coord[1]);

					double distance = Point2D.distance(
							locationToSort.getLatitude(),
							locationToSort.getLongitude(), clat, clng);

					route.min_distance = Math.min(route.min_distance, distance);
				}
			}
		}

		Collections.sort(items);

		notifyDataSetChanged();
	}

}