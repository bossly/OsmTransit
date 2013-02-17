package com.bossly.lviv.transit.data;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WebAPI extends DefaultHandler
{

	// http://overpass-api.de/api/interpreter?
	// data=relation(49.7422316,23.8623047,49.9529871,24.2056274)[route=trolleybus];out
	// meta;

	public static HashMap<Long, Node> nodes = null;
	public static HashMap<Long, Way> ways = null;

	public ArrayList<Route> parseTransitInfoByUrl(URL url)
	{

		nodes = new HashMap<Long, Node>();
		ways = new HashMap<Long, Way>();

		InputStream stream = null;
		routes = new ArrayList<Route>();

		try
		{
			stream = url.openStream();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			parser.parse(stream, this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (Exception e)
			{
			}
		}

		return routes;
	}

	/* XML parser */

	// helpers
	ArrayList<Route> routes = null;
	Route route = null;
	Node node = null;
	Way way = null;

	// ---

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException
	{

		if (node != null)
		{
			node.parse(qName, attributes);
		}
		else if (way != null)
		{
			way.parse(qName, attributes);
		}
		else if (route != null)
		{
			route.parse(qName, attributes);
		}
		else if (qName.equalsIgnoreCase("relation"))
		{
			route = new Route();
			route.parse(qName, attributes);
		}
		else if (qName.equalsIgnoreCase("node"))
		{
			node = new Node();
			node.parse(qName, attributes);
		}
		else if (qName.equalsIgnoreCase("way"))
		{
			way = new Way();
			way.parse(qName, attributes);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{

		if (qName.equalsIgnoreCase("relation") && route != null)
		{
			routes.add(route);
			route = null;
		}
		else if (node != null && qName.equalsIgnoreCase("node"))
		{
			nodes.put(node.id, node);
			node = null;
		}
		else if (way != null && qName.equalsIgnoreCase("way"))
		{
			ways.put(way.id, way);
			way = null;
		}
	}

}
