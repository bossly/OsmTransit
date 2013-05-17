package com.bossly.osm.transit.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.bossly.osm.transit.Region;
import com.bossly.osm.transit.WayRoute;
import com.bossly.osm.transit.WebAPI;
import com.bossly.osm.transit.engine.GeoUtils;
import com.bossly.osm.transit.engine.Route;

public class Transit {

	/*
	 * Comments:
	 * 
	 * - Make a search by building address and number - Make route search
	 * 
	 * https://play.google.com/store/apps/details?id=com.luitech.remindit
	 * https://github.com/mdavydov/UkrParser/
	 */

	// http://overpass-api.de/api/interpreter?
	// data=relation(49.7422316,23.8623047,49.9529871,24.2056274)[route=trolleybus];out
	// meta;
	// lviv(49.7422316,23.8623047,49.9529871,24.2056274) - yes
	// kyiv(50.61, 30.263, 50.281, 30.81) - yes
	// ternopil (49.5924, 25.5228, 49.5085, 25.6594) - no trasit

	public static Region Bounds_Lviv = new Region(49.7422316, 23.8623047,
			49.9529871, 24.2056274);

	// big lviv - 49.67, 23.774, 50.004, 24.291

	public static Region Bounds_Kyiv = new Region(50.281, 30.263, 50.61, 30.81);

	public static boolean DEBUG_LOG = true;
	public static boolean DEBUG_LOG_LEVEL1 = false;

	ArrayList<Route> routes = null;

	public ArrayList<Route> getCopyOfRoutes() {
		return new ArrayList<Route>(routes);
	}

	public ArrayList<Route> findRoutes(double lat, double lon, double lat2,
			double lon2) {

		int max_dist = 300; // meters

		ArrayList<Route> f1 = GeoUtils.filterRoutes(routes, lat, lon, max_dist);
		ArrayList<Route> f2 = GeoUtils.filterRoutes(f1, lat2, lon2, max_dist);

		return f2;
	}

	/* Data processing */

	public String downloadOsmData(Region region) {
		String boundbox = region.toString();
		String tags = "[\"route\"~\"trolleybus|tram|bus\"];>>;";
		String meta = URLEncoder.encode("out meta;");
		String temp_filename = "temp.osm";

		String link = String.format(
				"http://overpass-api.de/api/interpreter?data=relation(%s)%s%s",
				boundbox, tags, meta);

		// try to download files
		try {
			URL urlLink = new URL(link);
			URLConnection connection = urlLink.openConnection();
			InputStream stream = connection.getInputStream();

			FileOutputStream writer = new FileOutputStream(temp_filename);
			byte[] buffer = new byte[1024];
			int count = 0;

			do {
				writer.write(buffer, 0, count);
				count = stream.read(buffer);
			} while (count > 0);

			writer.flush();

			writer.close();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return temp_filename;
	}

	public void openData(String temp_filename) {

		WebAPI api = new WebAPI();
		routes = new ArrayList<Route>();

		File file = new File(temp_filename);
		URL url = null;

		try {
			url = file.toURI().toURL();
		} catch (Exception e) {
		}

		// get newest routes info
		if (url != null) {
			ArrayList<WayRoute> rts = api.parseTransitInfoByUrl(url);

			for (WayRoute wayRoute : rts) {

				Route route = new Route();
				route.id = wayRoute.id;
				route.name = wayRoute.getName();
				route.desc = wayRoute.genDescription();
				route.path = wayRoute.genPath();
				route.type = wayRoute.route;

				routes.add(route);
			}
		}

		// get newest routes info
		System.out.println("Routes loaded: " + routes.size());
	}

}
