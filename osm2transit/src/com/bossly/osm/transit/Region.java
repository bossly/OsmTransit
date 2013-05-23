package com.bossly.osm.transit;

public class Region {

	public String Name = null;
	public double Top, Left, Bottom, Right;

	public Region(double t, double l, double b, double r) {
		Top = b;
		Left = l;
		Bottom = t;
		Right = r;
	}

	// bottom,left,top,right
	public Region(String name, double b, double l, double t, double r) {
		Name = name;
		Top = b;
		Left = l;
		Bottom = t;
		Right = r;
	}

	public String bounds() {
		StringBuilder builder = new StringBuilder();
		builder.append(Double.toString(Top).replace(',', '.') + ",");
		builder.append(Double.toString(Left).replace(',', '.') + ",");
		builder.append(Double.toString(Bottom).replace(',', '.') + ",");
		builder.append(Double.toString(Right).replace(',', '.'));

		return builder.toString();
	}

	@Override
	public String toString() {

		if (Name == null)
			return bounds();

		return Name;
	}
}
