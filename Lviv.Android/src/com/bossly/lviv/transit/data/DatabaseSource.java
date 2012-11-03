package com.bossly.lviv.transit.data;

import java.util.ArrayList;

import com.bossly.lviv.transit.Route;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseSource {

	// Database fields
	private SQLiteDatabase database;

	private DatabaseHelper dbHelper;

	public DatabaseSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ArrayList<Route> getRoutes() {
		Cursor cursor = database.query(DatabaseHelper.TABLE_ROUTES, null, null,
				null, null, null, null, null);
		cursor.moveToFirst();

		ArrayList<Route> routes = new ArrayList<Route>();

		while (!cursor.isAfterLast()) {
			Route route = new Route();
			route.id = cursor.getLong(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_ID));
			route.name = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_ROUTE_NAME));
			route.type = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_ROUTE_TYPE));
			route.desc = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_ROUTE_DIRECTION));
			route.path = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_ROUTE_PATH));

			routes.add(route);

			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();

		return routes;
	}

	public long insertRoute(String name, String type, String desc, String path) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_ROUTE_NAME, name);
		values.put(DatabaseHelper.COLUMN_ROUTE_TYPE, type);
		values.put(DatabaseHelper.COLUMN_ROUTE_DIRECTION, desc);
		values.put(DatabaseHelper.COLUMN_ROUTE_PATH, path);

		return database.insert(DatabaseHelper.TABLE_ROUTES, null, values);
	}

	public void clear() {
		database.delete(DatabaseHelper.TABLE_ROUTES, null, null);
	}
}
