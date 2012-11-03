package com.bossly.lviv.transit;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route implements Comparable<Route>, Serializable {
	private static final long serialVersionUID = -8539225116230155969L;

	public long id;

	public String name;

	public String desc;

	public String type;

	public String path;

	public double min_distance;

	@Override
	public int compareTo(Route another) {

		int dist = (int) (min_distance - another.min_distance);

		if (dist == 0) {
			int numer1 = getNumber();
			int numer2 = another.getNumber();

			return numer1 - numer2;
		} else
			return dist;
	}

	final Pattern p = Pattern.compile("(\\d+)");

	private int getNumber() {
		int number = 0;
		Matcher m = p.matcher(name);

		if (m.find()) {
			try {
				number = Integer.parseInt(m.group(1));
			} catch (NumberFormatException e) {
			}
		}

		return number;
	}

	@Override
	public String toString() {
		return name + "[" + id + "]" + " - " + min_distance + " m";
	}
}
