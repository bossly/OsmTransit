package com.bossly.lviv.transit.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.v4.database.DatabaseUtilsCompat;
import android.util.Log;

public class RoutesDataProvider extends ContentProvider
{
	public static class DatabaseHelper extends SQLiteOpenHelper
	{
		/* Constants */

		private static final String DATABASE_NAME = "routes.db";

		private static final int DATABASE_VERSION = 5;

		public static final String TABLE_ROUTES = "routes";

		/* Columns */

		public static final String COLUMN_ID = "_id";

		public static final String COLUMN_ROUTE_ID = "uid";

		public static final String COLUMN_ROUTE_NAME = "name";

		public static final String COLUMN_ROUTE_TYPE = "type";

		public static final String COLUMN_ROUTE_DIRECTION = "description";

		public static final String COLUMN_ROUTE_PATH = "path";

		public static final String COLUMN_ROUTE_SEARCH = "search";

		/* Initialization */

		// Database creation sql statement
		private static final String DATABASE_CREATE = "create table " + TABLE_ROUTES + "(" + COLUMN_ID
				+ " integer primary key autoincrement, " + COLUMN_ROUTE_ID + " text not null, "
				+ COLUMN_ROUTE_NAME + " text, " + COLUMN_ROUTE_TYPE + " text, " + COLUMN_ROUTE_DIRECTION
				+ " text, " + COLUMN_ROUTE_SEARCH + " text, " + COLUMN_ROUTE_PATH + " text " + ");";

		private static final String DATABASE_POINT_CREATE = "create table points ("
				+ "_id integer primary key autoincrement, latitude FLOAT, longitude FLOAT, route_id INTEGER);";

		public DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database)
		{
			database.execSQL(DATABASE_CREATE);
			database.execSQL(DATABASE_POINT_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
			db.execSQL("DROP TABLE IF EXISTS points");

			onCreate(db);
		}
	}

	private final static UriMatcher MATCHER = new UriMatcher(0);
	private DatabaseHelper mDbHelper;

	static
	{
		MATCHER.addURI(RoutesContract.AUTHORITY, "routes", 0);
		MATCHER.addURI(RoutesContract.AUTHORITY, "routes/#", 1);
		MATCHER.addURI(RoutesContract.AUTHORITY, "points", 2);
		MATCHER.addURI(RoutesContract.AUTHORITY, "points/#", 3);
		MATCHER.addURI(RoutesContract.AUTHORITY, "routes/bounds/*", 4);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		String tableName = null;

		switch (MATCHER.match(uri))
		{
			case 0:
				tableName = DatabaseHelper.TABLE_ROUTES;
				break;

			case 1:
			{
				long id = ContentUris.parseId(uri);
				selection = DatabaseUtilsCompat.concatenateWhere(selection, "_id=" + id);
				tableName = DatabaseHelper.TABLE_ROUTES;
			}
				break;

			case 2:
				tableName = "points";
				break;

			case 3:
			{
				long id = ContentUris.parseId(uri);
				selection = DatabaseUtilsCompat.concatenateWhere(selection, "_id=" + id);
				tableName = "points";
			}

			default:
				throw new IllegalArgumentException("uri");
		}

		int result = mDbHelper.getWritableDatabase().delete(tableName, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);

		return result;
	}

	@Override
	public String getType(Uri uri)
	{
		switch (MATCHER.match(uri))
		{
			case 0:
				return RoutesContract.RouteData.DIR_MIME_TYPE;

			case 1:
				return RoutesContract.RouteData.ITEM_MIME_TYPE;

			case 2:
				return RoutesContract.PointData.DIR_MIME_TYPE;

			case 3:
				return RoutesContract.PointData.ITEM_MIME_TYPE;

			default:
				throw new IllegalArgumentException("uri");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		String tableName = null;

		switch (MATCHER.match(uri))
		{
			case 0:
				tableName = DatabaseHelper.TABLE_ROUTES;
				break;

			case 2:
				tableName = "points";
				break;

			default:
				throw new IllegalArgumentException("uri");
		}

		long id = mDbHelper.getWritableDatabase().insert(tableName, null, values);
		Uri resultUri = ContentUris.withAppendedId(RoutesContract.RouteData.CONTENT_URI, id);
		getContext().getContentResolver().notifyChange(resultUri, null);

		return resultUri;
	}

	@Override
	public boolean onCreate()
	{
		mDbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder)
	{
		String groupBy = null;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		switch (MATCHER.match(uri))
		{
			case 0:
				queryBuilder.setTables(DatabaseHelper.TABLE_ROUTES);
				break;

			case 1:
			{
				long id = ContentUris.parseId(uri);
				selection = DatabaseUtilsCompat.concatenateWhere(selection, "_id=" + id);
				queryBuilder.setTables(DatabaseHelper.TABLE_ROUTES);
			}
				break;

			case 2:
				queryBuilder.setTables("points");
				break;

			case 3:
			{
				long id = ContentUris.parseId(uri);
				selection = DatabaseUtilsCompat.concatenateWhere(selection, "_id=" + id);
				queryBuilder.setTables("points");
			}
			break;
			
			case 4:
			{
				String location = uri.getLastPathSegment();
				String[] coords = location.split(";");
				location = String.format("(latitude BETWEEN %s AND %s) AND (longitude BETWEEN %s AND %s)", 
						coords[0], coords[1], coords[2], coords[3]);
				
				queryBuilder.setTables("routes INNER JOIN points ON routes._id=points.route_id");				
				selection = DatabaseUtilsCompat.concatenateWhere(selection, location);
				groupBy = "routes._id";
			}
			break;

			default:
				throw new IllegalArgumentException("uri");
		}

		return queryBuilder.query(mDbHelper.getWritableDatabase(), projection, selection, selectionArgs, groupBy, null, sortOrder); 
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		String tableName = null;

		switch (MATCHER.match(uri))
		{
			case 0:
				tableName = DatabaseHelper.TABLE_ROUTES;
				break;

			case 1:
			{
				long id = ContentUris.parseId(uri);
				selection = DatabaseUtilsCompat.concatenateWhere(selection, "_id=" + id);
				tableName = DatabaseHelper.TABLE_ROUTES;
			}
				break;

			case 2:
				tableName = "points";
				break;

			case 3:
			{
				long id = ContentUris.parseId(uri);
				selection = DatabaseUtilsCompat.concatenateWhere(selection, "_id=" + id);
				tableName = "points";
			}

			default:
				throw new IllegalArgumentException("uri");
		}

		int result = mDbHelper.getWritableDatabase()
				.update(tableName, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);

		return result;
	}
}
