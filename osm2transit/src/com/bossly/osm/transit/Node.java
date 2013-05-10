package com.bossly.osm.transit;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Node extends DefaultHandler
{

	public long id;
	public double lat;
	public double lon;
	public String name;

	public void parse(String qName, Attributes attributes)
	{
		if (qName.equalsIgnoreCase("node"))
		{

			int iid = attributes.getIndex("id");
			id = Long.parseLong(attributes.getValue(iid));

			int ilat = attributes.getIndex("lat");
			int ilon = attributes.getIndex("lon");

			if (ilat >= 0 && ilon >= 0)
			{
				lat = Double.parseDouble(attributes.getValue(ilat));
				lon = Double.parseDouble(attributes.getValue(ilon));
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
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException
	{

		parse(qName, attributes);
	}
}
