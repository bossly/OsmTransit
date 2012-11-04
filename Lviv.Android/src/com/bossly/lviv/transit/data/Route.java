package com.bossly.lviv.transit.data;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import android.util.Log;

import com.bossly.lviv.transit.GeoUtils;
import com.bossly.lviv.transit.GeoUtils.Point2D;

public class Route {

	public long id;
	public String name;
	public String route;
	public String operator;
	public String from;
	public String to;

	public ArrayList<Long> stops = new ArrayList<Long>();
	public ArrayList<Long> ways = new ArrayList<Long>();

	public void parse(String qName, Attributes attributes) {

		if (qName.equalsIgnoreCase("relation")) {

			int iid = attributes.getIndex("id");
			id = Long.parseLong(attributes.getValue(iid));
		} else if (qName.equalsIgnoreCase("member")) {

			int ikey = attributes.getIndex("type");
			int ivalue = attributes.getIndex("ref");

			if (ikey >= 0 && ivalue >= 0) {
				String type = attributes.getValue(ikey);

				// load stops
				if (type.equalsIgnoreCase("node")) {
					// load node info
					String ref = attributes.getValue(ivalue);
					stops.add(Long.parseLong(ref));
				}
				// load ways
				else if (type.equalsIgnoreCase("way")) {
					// load node info
					String ref = attributes.getValue(ivalue);
					ways.add(Long.parseLong(ref));
				}
			}
		} else if (qName.equalsIgnoreCase("tag")) {

			int ikey = attributes.getIndex("k");
			int ivalue = attributes.getIndex("v");

			if (ikey >= 0 && ivalue >= 0) {
				String key = attributes.getValue(ikey);
				String value = attributes.getValue(ivalue);

				if (key.equalsIgnoreCase("name")) {
					name = value;
				} else if (key.equalsIgnoreCase("operator")) {
					operator = value;
				} else if (key.equalsIgnoreCase("from")) {
					from = value;
				} else if (key.equalsIgnoreCase("to")) {
					to = value;
				} else if (key.equalsIgnoreCase("route")) {
					route = value;
				}
			}
		}
	}

	public String genDescription() {
		StringBuilder builder = new StringBuilder("");
		String prev = "";

		for (Long w_ref : ways) {

			Way w = WebAPI.ways.get(w_ref);

			if (w.name != null && !w.name.equalsIgnoreCase(prev)) {
				if (builder.length() > 0)
					builder.append(" - ");

				builder.append(w.name + "");
			}

			if (w.name != null) {
				prev = w.name;
			}
		}

		return builder.toString();
	}

	public String genPath() {

		StringBuilder builder = new StringBuilder();

		// TODO: here is optimized for path generation.
		// point will be added if distance between prev - next
		// bigger that 300 meters
		float min_dist = 300;// meters

		Node prevNode = null;

		for (Long ref : ways) {

			Way way = WebAPI.ways.get(ref);

			for (Long nd : way.nodes) {

				Node node = WebAPI.nodes.get(nd);
				double dis = min_dist;

				if (prevNode != null) {
					dis = Point2D.distance(node.lat, node.lon, prevNode.lat,
							prevNode.lon);
				}

				if (dis >= min_dist) {
					builder.append(String.format("%f,%f;", node.lat, node.lon));
					prevNode = node;
				}
			}
		}

		return builder.toString();
	}

	public String genStops() {

		StringBuilder builder = new StringBuilder();

		for (Long stop : stops) {

			Node node = WebAPI.nodes.get(stop);
			builder.append(String.format("%f,%f;", node.lat, node.lon));
		}

		return builder.toString();
	}

	// 49.7422316,23.8623047,49.9529871,24.2056274
	public boolean insideCity(double left, double top, double right,
			double bottom) {

		boolean contain = true;

		for (Long ref : ways) {

			if(!contain)
				break;
			
			Way way = WebAPI.ways.get(ref);

			for (Long nd : way.nodes) {

				Node node = WebAPI.nodes.get(nd);

				if( !(node.lat > left && node.lat < right && node.lon > top && node.lon < bottom) )
				{
					Log.d(Route.class.getName(), "Ignored: " + this.name );
					contain = false;
					break;
				}
			}
		}

		return contain;
	}
}
