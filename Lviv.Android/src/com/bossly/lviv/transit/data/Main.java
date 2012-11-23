package com.bossly.lviv.transit.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.os.Environment;

public class Main
{

	public static boolean DEBUG_LOG = true;
	public static boolean DEBUG_LOG_LEVEL1 = false;

	static long startTime;
	static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Launch method.
	 * 
	 * @param args
	 */
	public static ArrayList<Route> LoadData(String savepath)
	{

		String boundbox = "49.7422316,23.8623047,49.9529871,24.2056274";
		String tags = "[\"route\"~\"trolleybus|tram|bus\"];>>;";
		String meta = URLEncoder.encode("out meta;");

		String link = String.format(
		    "http://overpass-api.de/api/interpreter?data=relation(%s)%s%s",
		    boundbox, tags, meta);

		startTime = System.currentTimeMillis();

		String temp_filename = Environment.getExternalStorageDirectory()
		    + "/temp.osm";

		// try to download file
		try
		{
			URL urlLink = new URL(link);
			URLConnection connection = urlLink.openConnection();
			InputStream stream = connection.getInputStream();

			FileOutputStream writer = new FileOutputStream(temp_filename);
			byte[] buffer = new byte[1024];
			int count = 0;

			do
			{
				writer.write(buffer, 0, count);
				count = stream.read(buffer);
			} while (count > 0);

			writer.flush();

			writer.close();
			stream.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		String[] args = new String[] { temp_filename };

		WebAPI api = new WebAPI();
		int count = args.length;
		ArrayList<Route> routes = new ArrayList<Route>();

		for (int i = 0; i < count; i++)
		{

			File file = new File(args[i]);
			URL url = null;

			try
			{
				url = file.toURI().toURL();
			}
			catch (Exception e)
			{
			}

			// get newest routes info
			if (url != null)
			{
				ArrayList<Route> rts = api.parseTransitInfoByUrl(url);
				routes.addAll(rts);
			}
		}

		// get newest routes info
		System.out.println("Routes loaded: " + routes.size());

		return routes;
	}

}
