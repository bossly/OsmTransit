package com.bossly.lviv.transit.data;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Way extends DefaultHandler {

	public long id;
	public String name;
	public ArrayList<Long> nodes = new ArrayList<Long>();

	public void parse( String qName, Attributes attributes )
	{
		if (qName.equalsIgnoreCase("way")) {
			
			int iid = attributes.getIndex("id");
			id = Long.parseLong(attributes.getValue(iid));
		}
		if (qName.equalsIgnoreCase("nd")) {

			int iref = attributes.getIndex("ref");

			if (iref >= 0) {
				String ref = attributes.getValue(iref);
				nodes.add(Long.parseLong(ref));
			}

		} else if (qName.equalsIgnoreCase("tag")) {

			int ikey = attributes.getIndex("k");
			int ivalue = attributes.getIndex("v");

			if (ikey >= 0 && ivalue >= 0) {
				String key = attributes.getValue(ikey);
				String value = attributes.getValue(ivalue);

				if (key.equalsIgnoreCase("name")) {
					name = value;
				}
			}
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		parse(qName, attributes);
	}

	public Way() {
	}

	public Way(String ref) {

		if (Main.DEBUG_LOG_LEVEL1)
			System.out.println("load Way: " + ref);

		String link = "http://www.openstreetmap.org/api/0.6/way/" + ref;

		InputStream stream = null;

		try {
			URL url = new URL(link);
			stream = url.openStream();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			parser.parse(stream, this);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
			}
		}
	}
}
