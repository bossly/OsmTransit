package com.bossly.lviv.transit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.bossly.lviv.transit.data.DatabaseSource;

public class RoutesLoader extends AsyncTaskLoader<List<Route>>
{
	public List<Route> m_data;

	public RoutesLoader(Context context)
	{
		super(context);

		m_data = null;
	}

	@Override
	protected void onStartLoading()
	{
		super.onStartLoading();

		if (takeContentChanged() || m_data == null || isReset())
		{
			forceLoad();
		} else
		{
			deliverResult(m_data);
		}
	}

	@Override
	protected void onStopLoading()
	{
		cancelLoad();
	}

	@Override
	public void deliverResult(List<Route> data)
	{
		if (isAbandoned())
		{
			data.addAll(m_data);
		} else if (isStarted())
		{
			m_data = data;
			super.deliverResult(data);
		}
	}

	@Override
	public List<Route> loadInBackground()
	{
		ArrayList<Route> list = new ArrayList<Route>();

		// list = loadFromFile(list);
		DatabaseSource db = new DatabaseSource(getContext());
		db.open();
		list = db.getRoutes();
		db.close();

		Collections.sort(list);

		return list;
	}
}