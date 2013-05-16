package com.bossly.osm.transit.test;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

import com.bossly.osm.transit.Route;

public class MapRoute implements MapPolygon {
	List<Coordinate> coords;

	public MapRoute(Route route) {

		// polyline
		coords = new ArrayList<Coordinate>();

		String sway = route.genPath();
		String[] path = sway.substring(0, sway.length() - 1).split(";");

		for (int j = 0; j < path.length; j++) {

			String[] coors = path[j].split(",");

			double lat = Double.parseDouble(coors[0]);
			double lon = Double.parseDouble(coors[1]);

			coords.add(new Coordinate(lat, lon));
		}
	}

	@Override
	public List<Coordinate> getPoints() {
		return coords;
	}

	@Override
	public void paint(Graphics g, List<Point> points) {

		int count = points.size();
		int[] pointsx = new int[count];
		int[] pointsy = new int[count];

		for (int i = 0; i < count; i++) {
			Point point = points.get(i);
			pointsx[i] = point.x;
			pointsy[i] = point.y;
		}

		g.drawPolyline(pointsx, pointsy, count);
	}

	@Override
	public void paint(Graphics g, Polygon polygon) {
		g.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
	}
}
