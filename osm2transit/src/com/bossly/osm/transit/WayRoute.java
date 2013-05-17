package com.bossly.osm.transit;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;

public class WayRoute
{

	public long id;
	public String name;
	public String route;
	public String operator;
	public String from;
	public String to;

	public ArrayList<Long> stops = new ArrayList<Long>();
	public ArrayList<Long> ways = new ArrayList<Long>();

	public void parse(String qName, Attributes attributes)
	{

		if (qName.equalsIgnoreCase("relation"))
		{

			int iid = attributes.getIndex("id");
			id = Long.parseLong(attributes.getValue(iid));
		}
		else if (qName.equalsIgnoreCase("member"))
		{

			int ikey = attributes.getIndex("type");
			int ivalue = attributes.getIndex("ref");
			int irole = attributes.getIndex("role");

			if (ikey >= 0 && ivalue >= 0)
			{
				String type = attributes.getValue(ikey);

				// load stops
				if (type.equalsIgnoreCase("node"))
				{
					// load node info
					String ref = attributes.getValue(ivalue);
					stops.add(Long.parseLong(ref));
				}
				// load ways
				else if (type.equalsIgnoreCase("way"))
				{

					// load node info
					String ref = attributes.getValue(ivalue);
					long way_id = Long.parseLong(ref);
					boolean skip = false;

					if (irole >= 0)
					{
						String str_role = attributes.getValue(irole);

						if (str_role != null && str_role.length() > 0)
							skip = true;
					}

					if (!skip)
						ways.add(way_id);

				}
			}
		}
		else if (qName.equalsIgnoreCase("tag"))
		{

			int ikey = attributes.getIndex("k");
			int ivalue = attributes.getIndex("v");

			if (ikey >= 0 && ivalue >= 0)
			{
				String key = attributes.getValue(ikey);
				String value = attributes.getValue(ivalue);

				if (key.equalsIgnoreCase("name"))
				{
					name = value;
				}
				else if (key.equalsIgnoreCase("operator"))
				{
					operator = value;
				}
				else if (key.equalsIgnoreCase("from"))
				{
					from = value;
				}
				else if (key.equalsIgnoreCase("to"))
				{
					to = value;
				}
				else if (key.equalsIgnoreCase("route"))
				{
					route = value;
				}
			}
		}
	}

	private final static Pattern NAME_PATTERN = Pattern.compile("(^.*[\\u2116]\\s\\S*)");

	public String getName()
	{
		Matcher matcher = NAME_PATTERN.matcher(name);

		if (matcher.find())
			return matcher.group(0);

		return name;
	}

	public String genDescription()
	{
		StringBuilder builder = new StringBuilder("");
		String prev = "";

		for (Long w_ref : ways)
		{

			Way w = WebAPI.ways.get(w_ref);

			if (w.name != null && !w.name.equalsIgnoreCase(prev))
			{
				if (builder.length() > 0)
					builder.append(" - ");

				builder.append(w.name + "");
			}

			if (w.name != null)
			{
				prev = w.name;
			}
		}

		return builder.toString().replace(" вулиця", ""); // TODO WTF!!!
		// return builder.toString();
	}

	public ArrayList<Node> getNodes()
	{
		ArrayList<Node> result = new ArrayList<Node>();

		// TODO: here is optimized for path generation.
		// point will be added if distance between prev - next
		// bigger that 150 meters
		float min_dist = 150;// meters

		Node prevNode = null;
		long lastNode = -1;
		ArrayList<Node> way_nodes = new ArrayList<Node>();
		int way_index = 0;

		for (Long ref : ways)
		{
			Way way = WebAPI.ways.get(ref);

			// check if need to reverse
			long last = way.nodes.size() > 0 ? way.nodes.get(way.nodes.size() - 1) : 0;
			boolean reverse = false;
			int last_index = way_nodes.size();

			if (last == lastNode)
			{
				reverse = true;
			}
			else if (way_index == 1 && way_nodes.size() > 0 && last == way_nodes.get(0).id)
			{
				ArrayList<Node> revertedL = new ArrayList<Node>();

				// revert way_nodes array
				for (int i = way_nodes.size() - 1; i >= 0; i--)
				{
					revertedL.add(way_nodes.get(i));
				}

				way_nodes = revertedL;
				reverse = true;
			}

			for (Long nd : way.nodes)
			{

				Node node = WebAPI.nodes.get(nd);

				if (reverse && way_nodes.size() != last_index)
					way_nodes.add(last_index, node);
				else
					way_nodes.add(node);
			}

			lastNode = way_nodes.size() > 0 ? way_nodes.get(way_nodes.size() - 1).id : -1;
			way_index++;
		}

		for (Node node : way_nodes)
		{

			double dis = min_dist;

			if (prevNode != null)
			{
			//	dis = Point2D.distance(node.lat, node.lon, prevNode.lat, prevNode.lon);
			}

			if (dis >= min_dist)
			{
				result.add(node);
				prevNode = node;
			}
		}

		return result;
	}

	public String genPath()
	{

		StringBuilder builder = new StringBuilder();

		// TODO: here is optimized for path generation.
		// point will be added if distance between prev - next
		// bigger that 150 meters
		float min_dist = 150;// meters

		Node prevNode = null;
		long lastNode = -1;
		ArrayList<Node> way_nodes = new ArrayList<Node>();
		int way_index = 0;

		for (Long ref : ways)
		{

			Way way = WebAPI.ways.get(ref);

			// check if need to reverse
			long last = way.nodes.size() > 0 ? way.nodes.get(way.nodes.size() - 1) : 0;
			boolean reverse = false;
			int last_index = way_nodes.size();

			if (last == lastNode)
			{
				reverse = true;
			}
			else if (way_index == 1 && way_nodes.size() > 0 && last == way_nodes.get(0).id)
			{
				ArrayList<Node> revertedL = new ArrayList<Node>();

				// revert way_nodes array
				for (int i = way_nodes.size() - 1; i >= 0; i--)
				{
					revertedL.add(way_nodes.get(i));
				}

				way_nodes = revertedL;
				reverse = true;
			}

			for (Long nd : way.nodes)
			{

				Node node = WebAPI.nodes.get(nd);

				if (reverse && way_nodes.size() != last_index)
					way_nodes.add(last_index, node);
				else
					way_nodes.add(node);
			}

			lastNode = way_nodes.size() > 0 ? way_nodes.get(way_nodes.size() - 1).id : -1;
			way_index++;
		}

		for (Node node : way_nodes)
		{

			double dis = min_dist;

			if (prevNode != null)
			{
				//dis = Point2D.distance(node.lat, node.lon, prevNode.lat, prevNode.lon);
			}

			if (dis >= min_dist)
			{
				builder.append(String.format(Locale.US, "%f,%f;", node.lat, node.lon));
				prevNode = node;
			}
		}

		return builder.toString();
	}

	public String genStops()
	{

		StringBuilder builder = new StringBuilder();

		for (Long stop : stops)
		{

			Node node = WebAPI.nodes.get(stop);
			//builder.append(node.name);		
			//builder.append(";");
			builder.append(String.format("%f,%f", node.lat, node.lon));
		}

		return builder.toString();
	}

	// 49.7422316,23.8623047,49.9529871,24.2056274
	public boolean insideCity(double left, double top, double right, double bottom)
	{

		boolean contain = true;

		for (Long ref : ways)
		{

			if (!contain)
				break;

			Way way = WebAPI.ways.get(ref);

			for (Long nd : way.nodes)
			{

				Node node = WebAPI.nodes.get(nd);

				if (!(node.lat > left && node.lat < right && node.lon > top && node.lon < bottom))
				{
					contain = false;
					break;
				}
			}
		}

		return contain;
	}

	@Override
	public String toString() {
		return getName();
	}
}
