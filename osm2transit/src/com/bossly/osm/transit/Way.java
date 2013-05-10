package com.bossly.osm.transit;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Way extends DefaultHandler
{
	public long id;
	public String name;

	public ArrayList<Long> nodes = new ArrayList<Long>();

	public void parse(String qName, Attributes attributes)
	{
		if (qName.equalsIgnoreCase("way"))
		{

			int iid = attributes.getIndex("id");
			id = Long.parseLong(attributes.getValue(iid));
		}
		if (qName.equalsIgnoreCase("nd"))
		{

			int iref = attributes.getIndex("ref");

			if (iref >= 0)
			{
				String ref = attributes.getValue(iref);
				nodes.add(Long.parseLong(ref));
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
