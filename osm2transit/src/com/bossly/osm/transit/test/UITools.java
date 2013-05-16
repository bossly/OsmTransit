package com.bossly.osm.transit.test;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import com.bossly.osm.transit.Route;

public class UITools {

	public static void showRouteOnGoogleMaps(Route route) {

		StringBuilder builder = new StringBuilder();
		builder.append("http://maps.googleapis.com/maps/api/staticmap");
		builder.append("?size=" + 400 + "x" + 500);
		builder.append("&path=color:0x0000ff" + URLEncoder.encode("|weight:5"));
		String sway = route.genPath();

		String[] path = sway.substring(0, sway.length() - 1).split(";");

		for (int j = 0; j < path.length; j++) {
			String[] coors = path[j].split(",");

			double lat = Double.parseDouble(coors[0]);
			double lon = Double.parseDouble(coors[1]);

			builder.append(URLEncoder.encode("|" + lat + "," + lon));
		}

		builder.append("&sensor=true");

		try {
			openWebpage(new URI(builder.toString()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
