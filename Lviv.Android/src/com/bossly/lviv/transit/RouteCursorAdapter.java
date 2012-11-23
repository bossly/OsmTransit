package com.bossly.lviv.transit;

import com.bossly.lviv.transit.data.RoutesContract;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RouteCursorAdapter extends CursorAdapter
{
	private int _nameIndex;
	private int _typeIndex;
	private int _numberIndex;
	private int _descIndex;
	
	public RouteCursorAdapter(Context context, Cursor c, int flags)
	{
		super(context, c, flags);
		
		_nameIndex = c.getColumnIndex(RoutesContract.RouteData.NAME);
		_typeIndex = c.getColumnIndex(RoutesContract.RouteData.TYPE);
		_numberIndex = c.getColumnIndex(RoutesContract.RouteData.UID);
		_descIndex = c.getColumnIndex(RoutesContract.RouteData.DIRECTION);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		
		text1.setText(cursor.getString(_nameIndex));
		text2.setText(cursor.getString(_descIndex));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.item_route, null);
	}
}
