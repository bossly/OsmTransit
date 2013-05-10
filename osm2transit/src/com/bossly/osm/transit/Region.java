package com.bossly.osm.transit;

public class Region {

	public double Top, Left, Bottom, Right;

	public Region(double t, double l, double b, double r) {
		Top = t;
		Left = l;
		Bottom = b;
		Right = r;
	}

	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(Double.toString(Top).replace(',', '.') + ",");
		builder.append(Double.toString(Left).replace(',', '.') + ",");
		builder.append(Double.toString(Bottom).replace(',', '.') + ",");
		builder.append(Double.toString(Right).replace(',', '.'));
		
		return builder.toString();
	}
}
