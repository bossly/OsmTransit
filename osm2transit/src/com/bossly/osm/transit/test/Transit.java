package com.bossly.osm.transit.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bossly.osm.transit.Region;
import com.bossly.osm.transit.Route;
import com.bossly.osm.transit.WebAPI;

public class Transit {

	/*
	 * Comments:
	 * 
	 * - Make a search by building address and number - Make route search
	 * 
	 * https://play.google.com/store/apps/details?id=com.luitech.remindit
	 * https://github.com/mdavydov/UkrParser/
	 */

	// http://overpass-api.de/api/interpreter?
	// data=relation(49.7422316,23.8623047,49.9529871,24.2056274)[route=trolleybus];out
	// meta;
	// lviv(49.7422316,23.8623047,49.9529871,24.2056274) - yes
	// kyiv(50.61, 30.263, 50.281, 30.81) - yes
	// ternopil (49.5924, 25.5228, 49.5085, 25.6594) - no trasit

	public static Region Bounds_Lviv = new Region(49.7422316, 23.8623047, 49.9529871,
			24.2056274);

	// big lviv - 49.67, 23.774, 50.004, 24.291

	public static Region Bounds_Kyiv = new Region(50.281, 30.263, 50.61, 30.81);

	public static boolean DEBUG_LOG = true;
	public static boolean DEBUG_LOG_LEVEL1 = false;

	ArrayList<Route> routes = null;

	public ArrayList<Route> findRoutes(String direction) {

		// parse text input
		int max_dist = 300; // meters

		String[] coords = direction.split(",");

		double lat = Double.parseDouble(coords[0].trim());
		double lon = Double.parseDouble(coords[1].trim());
		double lat2 = Double.parseDouble(coords[2].trim());
		double lon2 = Double.parseDouble(coords[3].trim());

		ArrayList<Route> f1 = GeoUtils.filterRoutes(routes, lat, lon, max_dist);
		ArrayList<Route> f2 = GeoUtils.filterRoutes(f1, lat2, lon2, max_dist);

		return f2;
	}

	/* Data processing */

	public String downloadOsmData(Region region) {
		String boundbox = region.toString();
		String tags = "[\"route\"~\"trolleybus|tram|bus\"];>>;";
		String meta = URLEncoder.encode("out meta;");
		String temp_filename = "temp.osm";

		String link = String.format(
				"http://overpass-api.de/api/interpreter?data=relation(%s)%s%s",
				boundbox, tags, meta);

		// try to download files
		try {
			URL urlLink = new URL(link);
			URLConnection connection = urlLink.openConnection();
			InputStream stream = connection.getInputStream();

			FileOutputStream writer = new FileOutputStream(temp_filename);
			byte[] buffer = new byte[1024];
			int count = 0;

			do {
				writer.write(buffer, 0, count);
				count = stream.read(buffer);
			} while (count > 0);

			writer.flush();

			writer.close();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return temp_filename;
	}

	public void openData(String temp_filename) {

		WebAPI api = new WebAPI();
		routes = new ArrayList<Route>();

		File file = new File(temp_filename);
		URL url = null;

		try {
			url = file.toURI().toURL();
		} catch (Exception e) {
		}

		// get newest routes info
		if (url != null) {
			ArrayList<Route> rts = api.parseTransitInfoByUrl(url);
			routes.addAll(rts);
		}

		// get newest routes info
		System.out.println("Routes loaded: " + routes.size());
	}

	public void saveToFile(String filepath)
			throws ParserConfigurationException, TransformerException {

		// create the xml document builder factory object
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation domImpl = builder.getDOMImplementation();

		// create a document with the default namespace
		// and a root node
		Document xmlDoc = domImpl.createDocument(null, "routes", null);

		// get the root element
		Element routesElement = xmlDoc.getDocumentElement();

		for (Route route : routes) {

			if (route == null || route.name == null)
				continue;

			Element routeElement = xmlDoc.createElement("route");

			// id
			Element rId = xmlDoc.createElement("id");
			rId.setTextContent("" + route.id);
			routeElement.appendChild(rId);

			// name
			Element rName = xmlDoc.createElement("name");
			rName.setTextContent(route.name);
			routeElement.appendChild(rName);

			// desc
			Element rDesc = xmlDoc.createElement("desc");
			rDesc.setTextContent(route.genDescription());
			routeElement.appendChild(rDesc);

			// type
			Element rType = xmlDoc.createElement("type");
			if (route.route != null)
				rType.setTextContent(route.route);
			routeElement.appendChild(rType);

			Element element = xmlDoc.createElement("stops");
			element.setTextContent(route.genStops());
			routeElement.appendChild(element);

			// <path>49.83959212773939,23.994769489288274;</path>
			// TODO: gen path with nodes
			element = xmlDoc.createElement("path");
			// element.setTextContent(route.genPath());
			routeElement.appendChild(element);

			routesElement.appendChild(routeElement);
		}

		// save to file
		DOMSource source = new DOMSource(xmlDoc);
		StreamResult result = new StreamResult(filepath);

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();

		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		transformer.transform(source, result);
	}

}
