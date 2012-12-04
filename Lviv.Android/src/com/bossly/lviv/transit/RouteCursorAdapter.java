package com.bossly.lviv.transit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bossly.lviv.transit.data.RoutesContract;
import com.bossly.lviv.transit.utils.CommonUtils;

public class RouteCursorAdapter extends CursorAdapter
{
	private int _nameIndex;
	private int _descIndex;

	private String[] mFilters = null;

	public RouteCursorAdapter(Context context, Cursor c, int flags)
	{
		super(context, c, flags);

		_nameIndex = c.getColumnIndex(RoutesContract.RouteData.NAME);
		c.getColumnIndex(RoutesContract.RouteData.TYPE);
		c.getColumnIndex(RoutesContract.RouteData.UID);
		_descIndex = c.getColumnIndex(RoutesContract.RouteData.DIRECTION);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);

		text1.setText(cursor.getString(_nameIndex));

		if (mFilters == null)
		{
			text2.setText(cursor.getString(_descIndex));
		}
		else
		{
			text2.setText(CommonUtils.highlight(cursor.getString(_descIndex), mFilters, Color.YELLOW));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.item_route, null);
	}

	public String[] getFilterHighlights()
	{
		return mFilters;
	}

	public void setFilterHighlight(String[] filters)
	{
		this.mFilters = filters;
	}
}
