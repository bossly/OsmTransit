package com.bossly.osm.transit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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

public class Main {

	public static boolean DEBUG_LOG = true;
	public static boolean DEBUG_LOG_LEVEL1 = false;

	public static float per_total = 0;
	public static float per_item = 100;

	static long startTime;
	static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private static String elapsedTime() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		return dateFormat.format(new Date(elapsedTime));
	}

	public static void progress() {
		int prev = (int) per_total;

		per_total += per_item;

		int now = (int) per_total;

		if (now != prev) {
			System.out.println("Progress[" + per_total + "] time: "
					+ elapsedTime());
		}
	}

	/**
	 * Launch method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String boundbox = "49.7422316,23.8623047,49.9529871,24.2056274";
		String tags = "[\"route\"~\"trolleybus|tram|bus\"];>>;";
		String meta = URLEncoder.encode("out meta;");

		String link = String.format(
				"http://overpass-api.de/api/interpreter?data=relation(%s)%s%s",
				boundbox, tags, meta);

		startTime = System.currentTimeMillis();

		String temp_filename = "temp.osm";
		// *
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
		// */
		args = new String[] { temp_filename };

		WebAPI api = new WebAPI();
		int count = args.length;
		ArrayList<Route> routes = new ArrayList<Route>();
		per_item = per_item / count;

		for (int i = 0; i < count; i++) {

			File file = new File(args[i]);
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

			progress();

			// if (DEBUG_LOG)
			// System.out.println("parsed: " + file.getAbsolutePath());
		}

		// get newest routes info
		System.out.println("Routes loaded: " + routes.size());

		// save to formatted file
		try {
			saveToFile(routes, "route.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveToFile(ArrayList<Route> routes, String filepath)
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
		Date today = new Date();

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

			// <start>6:05</start>
			Element element = xmlDoc.createElement("start");
			routeElement.appendChild(element);

			// <end>23:19</end>
			element = xmlDoc.createElement("end");
			routeElement.appendChild(element);

			// <path>49.83959212773939,23.994769489288274;</path>
			// TODO: gen path with nodes
			element = xmlDoc.createElement("path");
			// element.setTextContent(route.genStops());
			routeElement.appendChild(element);

			// <modified>1331818240813</modified>
			element = xmlDoc.createElement("modified");
			element.setTextContent(today.getTime() + "");
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
