package com.bossly.osm.transit.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class MapMarkerPoint implements MapMarker {

	double _lat;
	double _lon;

	public MapMarkerPoint(double lat, double lon) {
		_lat = lat;
		_lon = lon;
	}

	@Override
	public double getLat() {
		// TODO Auto-generated method stub
		return _lat;
	}

	@Override
	public double getLon() {
		// TODO Auto-generated method stub
		return _lon;
	}

	@Override
	public void paint(Graphics arg0, Point point) {

		int radius = 5;
		int x = point.x;
		int y = point.y;

		arg0.setColor(Color.RED);
		arg0.fillOval(x - radius / 2, y - radius / 2, radius, radius);
	}

}
