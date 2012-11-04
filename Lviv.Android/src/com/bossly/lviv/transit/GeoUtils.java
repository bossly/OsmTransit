package com.bossly.lviv.transit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.location.Location;

public class GeoUtils {

	static Random r = new Random();

	public static ArrayList<Route> filterRoutes2(List<Route> data, double lat,
			double lng, double max_dist) {

		ArrayList<Route> nearest = null;

		if (data != null) {
			nearest = new ArrayList<Route>();

			for (Route route : data) {

				// calc short way to route
				if (route.path != null && route.path.length() > 0) {
					String[] points = route.path.split(";");

					for (int j = 0, count = points.length; j < count; j++) {
						String[] coord = points[j].split(",");

						double clat = Double.parseDouble(coord[0]);
						double clng = Double.parseDouble(coord[1]);

						double distance = Point2D
								.distance(lat, lng, clat, clng);

						route.min_distance = distance;
						// if (distance >= 0 && distance < max_dist) {
						// nearest.add(route);
						// break;
						// }
					}
				}

				route.min_distance = r.nextInt(100); // unknown
				nearest.add(route);
			}

		}

		return nearest;
	}

	public static ArrayList<Route> filterRoutes(List<Route> data, double lat,
			double lng, double max_dist) {

		ArrayList<Route> nearest = new ArrayList<Route>();

		if (data != null) {
			for (Route route : data) {

				// calc short way to route
				if (route.path != null) {
					String[] points = route.path.split(";");
					double pclat = 0.0;
					double pclng = 0.0;

					for (int j = 0, count = points.length; j < count; j++) {
						String[] coord = points[j].split(",");

						double clat = Double.parseDouble(coord[0]);
						double clng = Double.parseDouble(coord[1]);

						if (j != 0) {
							double distance = GeoUtils.distanceToSegment(lat,
									lng, clat, clng, pclat, pclng);

							if (distance >= 0 && distance < max_dist) {
								nearest.add(route);
								break;
							}
						}

						pclat = clat;
						pclng = clng;
					}

				}

			}

		}

		return nearest;
	}

	// get distance in meters

	public static double distanceToSegment(double x3, double y3, double x1,
			double y1, double x2, double y2) {
		final Point2D pt = Point2D.Double(x3, y3);
		final Point2D p1 = Point2D.Double(x1, y1);
		final Point2D p2 = Point2D.Double(x2, y2);

		return distanceToSegment(p1, p2, pt);
	}

	/**
	 * Returns the distance of p3 to the segment defined by p1,p2;
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @param p3
	 *            Point to which we want to know the distance of the segment
	 *            defined by p1,p2
	 * @return The distance of p3 to the segment defined by p1,p2
	 */
	public static double distanceToSegment(Point2D p1, Point2D p2, Point2D p3) {

		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();

		if ((xDelta == 0) && (yDelta == 0)) {
			return -1;
			// throw new IllegalArgumentException(
			// "p1 and p2 cannot be the same point" );
		}

		final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1
				.getY()) * yDelta)
				/ (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0) {
			closestPoint = p1;
		} else if (u > 1) {
			closestPoint = p2;
		} else {
			closestPoint = Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u
					* yDelta);
		}

		return closestPoint.distance(p3);
	}

	public static class Point2D {
		double _x;

		double _y;

		public static Point2D Double(double x, double y) {
			Point2D p = new Point2D();
			p._x = x;
			p._y = y;

			return p;
		}

		public double distance(Point2D p3) {
			return distance(_x, _y, p3._x, p3._y);
		}

		public double getY() {
			return _y;
		}

		public double getX() {
			return _x;
		}

		public static double distance(double clat1, double clon1, double lat,
				double lon) {
			double R = 6366000; // km
			double dLat = Math.toRadians(clat1);
			double dLon = Math.toRadians(clon1);
			double lat1 = Math.toRadians(lat);
			double lat2 = Math.toRadians(lon);

			double t1 = Math.cos(dLat) * Math.cos(dLon) * Math.cos(lat1)
					* Math.cos(lat2);
			double t2 = Math.cos(dLat) * Math.sin(dLon) * Math.cos(lat1)
					* Math.sin(lat2);
			double t3 = Math.sin(dLat) * Math.sin(lat1);
			double tt = Math.acos(t1 + t2 + t3);

			double distance = R * tt;

			return distance;
		}

	}

	/* Determine location */

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	public static boolean isBetterLocation(Location location,
			Location currentBestLocation, long max_min) {

		if (currentBestLocation == null) {

			// Check whether the new location fix is newer or
			// older
			Date now = Calendar.getInstance().getTime();
			long timeDelta = location.getTime() - now.getTime()
					- now.getTimezoneOffset() * 60000;
			boolean isNewer = timeDelta > 0;

			// A new location is always better than no location
			return isNewer;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > max_min;
		boolean isSignificantlyOlder = timeDelta < -max_min;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current
		// location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older,
			// it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less
		// accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same
		// provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of
		// timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}

		return provider1.equals(provider2);
	}

}
