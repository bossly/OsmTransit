package com.bossly.lviv.transit.data;

import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;

public class RoutesContract
{
	public static final String AUTHORITY = "com.bossly.lviv.transit.provider";
	
	public static class RouteData implements BaseColumns
	{
		public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/routes");
		
		public final static String DIR_MIME_TYPE = "vnd.android.cursor.dir/com.bossly.lviv.transit.provider.routes";

		public final static String ITEM_MIME_TYPE = "vnd.android.cursor.item/com.bossly.lviv.transit.provider.routes";
	  
	  public static final String UID = "uid";
	  
	  public static final String NAME = "name";
	  
	  public static final String TYPE = "type";
	  
	  public static final String DIRECTION = "description";
	  
	  public static final String PATH = "path";
	  
		public static final String SEARCH = "search";
	}

	public static class PointData implements BaseColumns
	{
		public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/points");
		
		public final static String DIR_MIME_TYPE = "vnd.android.cursor.dir/com.bossly.lviv.transit.provider.points";

		public final static String ITEM_MIME_TYPE = "vnd.android.cursor.item/com.bossly.lviv.transit.provider.points";

	  public static final String ROUTE_ID = "route_id";

	  public static final String LATITUDE = "latitude";
	  
		public static final String LONGITUDE = "longitude";
	}
}
