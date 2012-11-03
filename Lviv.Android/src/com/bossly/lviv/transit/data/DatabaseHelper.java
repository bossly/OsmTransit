package com.bossly.lviv.transit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
  /* Constants */
  
  private static final String DATABASE_NAME = "routes.db";
  
  private static final int DATABASE_VERSION = 1;
  
  public static final String TABLE_ROUTES = "routes";
  
  /* Columns */
  
  public static final String COLUMN_ID = "_id";
  
  public static final String COLUMN_ROUTE_ID = "uid";
  
  public static final String COLUMN_ROUTE_NAME = "name";
  
  public static final String COLUMN_ROUTE_TYPE = "type";
  
  public static final String COLUMN_ROUTE_DIRECTION = "description";
  
  public static final String COLUMN_ROUTE_PATH = "path";
  
  /* Initialization */
  
  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table " + TABLE_ROUTES + "(" + COLUMN_ID
      + " integer primary key autoincrement, " + COLUMN_ROUTE_ID + " text not null, " + COLUMN_ROUTE_NAME + " text, "
      + COLUMN_ROUTE_TYPE + " text, " + COLUMN_ROUTE_DIRECTION + " text, " + COLUMN_ROUTE_PATH + " text "
      + ");";
  
  public DatabaseHelper( Context context )
  {
    super( context, DATABASE_NAME, null, DATABASE_VERSION );
  }
  
  @Override
  public void onCreate( SQLiteDatabase database )
  {
    database.execSQL( DATABASE_CREATE );
  }
  
  @Override
  public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
  {
    Log.w( DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
        + ", which will destroy all old data" );
    
    db.execSQL( "DROP TABLE IF EXISTS " + TABLE_ROUTES );
    
    onCreate( db );
  }
  
}