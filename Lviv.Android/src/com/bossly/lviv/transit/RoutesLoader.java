package com.bossly.lviv.transit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml.Encoding;

import com.bossly.lviv.transit.data.DatabaseSource;

public class RoutesLoader extends AsyncTaskLoader<List<Route>> {

	private static final String PATH_TO_ASSETS = "route.zip";

	public List<Route> m_data;

	public RoutesLoader(Context context) {
		super(context);

		m_data = null;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();

		if (takeContentChanged() || m_data == null || isReset()) {
			forceLoad();
		} else {
			deliverResult(m_data);
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	public void deliverResult(List<Route> data) {

		if (isAbandoned()) {
			data.addAll(m_data);
		} else if (isStarted()) {
			m_data = data;
			super.deliverResult(data);
		}
	}

	@Override
	public List<Route> loadInBackground() {
		ArrayList<Route> list = new ArrayList<Route>();

		// list = loadFromFile(list);
		DatabaseSource db = new DatabaseSource(getContext());
		db.open();
		list = db.getRoutes();
		db.close();

		Collections.sort(list);

		return list;
	}

	private ArrayList<Route> loadFromFile(ArrayList<Route> list) {
		File file = new File(getContext().getCacheDir(), "transit_data.zip");

		// load data from cache and parsing
		try {
			ZipFile zipfile = new ZipFile(file);
			ZipEntry entry = zipfile.getEntry("route.xml");

			InputStream stream = zipfile.getInputStream(entry);

			list = loadFromCache(stream);
		} catch (FileNotFoundException e) {
			list = null;
		} catch (ZipException e) {
			list = null;
		} catch (IOException e) {
			list = null;
		}

		if (list == null) {
			// load from assets
			try {
				InputStream asset_stream = getContext().getResources()
						.getAssets().open(PATH_TO_ASSETS);

				ZipInputStream zip_stream = new ZipInputStream(asset_stream);

				ZipEntry entry;

				while ((entry = zip_stream.getNextEntry()) != null) {

					if (entry.getName().equalsIgnoreCase("route.xml")) {

						int size;
						byte[] buffer = new byte[2048];

						ByteArrayOutputStream bos = new ByteArrayOutputStream();

						while ((size = zip_stream
								.read(buffer, 0, buffer.length)) != -1) {
							bos.write(buffer, 0, size);
						}

						bos.flush();
						bos.close();

						ByteArrayInputStream stream = new ByteArrayInputStream(
								bos.toByteArray());
						list = loadFromCache(stream);
					}
				}
			} catch (IOException e) {
				list = new ArrayList<Route>();
			}
		}
		return list;
	}

	private ArrayList<Route> loadFromCache(InputStream stream) {
		ArrayList<Route> list = null;

		// load data from cache and parsing
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(stream, Encoding.UTF_8.name());

			int evType = parser.getEventType();

			Route item = null;

			while (evType != XmlPullParser.END_DOCUMENT) {
				evType = parser.getEventType();

				if (evType == XmlPullParser.START_TAG) {
					String name = parser.getName();

					if (name.equals("route")) {
						item = new Route();

						if (list == null) {
							list = new ArrayList<Route>();
						}

						list.add(item);
					} else if (name.equalsIgnoreCase("id")) {
						if (item != null) {
							item.id = Long.parseLong(parser.nextText());
						}
					} else if (name.equalsIgnoreCase("name")) {
						if (item != null) {
							item.name = parser.nextText();
						}
					} else if (name.equalsIgnoreCase("desc")) {
						if (item != null) {
							item.desc = parser.nextText();
						}
					} else if (name.equalsIgnoreCase("path")) {
						if (item != null) {
							item.path = parser.nextText();
						}
					} else if (name.equalsIgnoreCase("type")) {
						if (item != null) {
							item.type = parser.nextText();
						}
					}
				}

				parser.next();
			}

			stream.close();
			factory = null;
			parser = null;
			stream = null;
		} catch (XmlPullParserException xppe) {
			xppe.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return list;
	}
}