package com.bossly.osm.transit.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewer.ZOOM_BUTTON_STYLE;

import com.bossly.osm.transit.engine.Route;

public class Application {

	/* views */
	private JFrame frameView;
	private JList listView;
	private JMapViewer mapView;

	/* variables */
	private String filename = "temp.osm";
	private Transit transit = new Transit();
	private Coordinate startPoint = null;
	private Coordinate endPoint = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Application window = new Application();
					window.frameView.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Application() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frameView = new JFrame();
		frameView.setBounds(100, 100, 661, 514);
		frameView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frameView.getContentPane().add(panel, BorderLayout.NORTH);

		JButton btnDownload = new JButton("Download");
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				downloadData();
			}
		});

		JButton btnLoadFromFile = new JButton("Load from file");
		btnLoadFromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				File file = new File(filename);

				if (!file.exists()) {
					downloadData();
				} else {
					transit.openData(filename);
					reloadList();
				}
			}
		});

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel.createSequentialGroup().addContainerGap()
						.addComponent(btnDownload)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnLoadFromFile).addGap(184)));
		gl_panel.setVerticalGroup(gl_panel
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_panel.createSequentialGroup()
								.addGap(5)
								.addGroup(
										gl_panel.createParallelGroup(
												Alignment.BASELINE)
												.addComponent(btnDownload)
												.addComponent(btnLoadFromFile))));
		panel.setLayout(gl_panel);

		JPanel panel_2 = new JPanel();
		frameView.getContentPane().add(panel_2, BorderLayout.CENTER);

		JList list_1 = new JList();
		list_1.setValueIsAdjusting(true);

		list_1.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent evt) {

				JList list = (JList) evt.getSource();
				Route route = (Route) list.getSelectedValue();

				System.out.println("Route id: " + route.id);
				
				showRouteOnMap(route);
			}
		});

		listView = list_1;

		JScrollPane scroll = new JScrollPane(list_1);

		mapView = new JMapViewer();
		mapView.setMapMarkerVisible(true);
		mapView.setZoomButtonStyle(ZOOM_BUTTON_STYLE.HORIZONTAL);
		mapView.setScrollWrapEnabled(true);

		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(gl_panel_2.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_2
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scroll, GroupLayout.PREFERRED_SIZE, 232,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(mapView, GroupLayout.DEFAULT_SIZE, 411,
								Short.MAX_VALUE).addContainerGap()));
		gl_panel_2
				.setVerticalGroup(gl_panel_2
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panel_2
										.createSequentialGroup()
										.addGroup(
												gl_panel_2
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																mapView,
																GroupLayout.DEFAULT_SIZE,
																421,
																Short.MAX_VALUE)
														.addGroup(
																gl_panel_2
																		.createSequentialGroup()
																		.addGap(6)
																		.addComponent(
																				scroll,
																				GroupLayout.DEFAULT_SIZE,
																				415,
																				Short.MAX_VALUE)))
										.addContainerGap()));
		panel_2.setLayout(gl_panel_2);

		mapView.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				placeSearchPoint(arg0.getPoint());
			}
		});

		mapView.setZoom(11);
		mapView.setCenter(new Point(297155,178167));
	}

	/* Helper methods */

	private void placeSearchPoint(Point p) {

		Coordinate coor = mapView.getPosition(p);

		mapView.removeAllMapMarkers();
		mapView.removeAllMapPolygons();

		if (startPoint == null) {
			startPoint = coor;

			mapView.addMapMarker(new MapMarkerPoint(startPoint.getLat(),
					startPoint.getLon()));

		} else if (endPoint == null) {
			endPoint = coor;

			// find routes
			ArrayList<Route> routes = transit.findRoutes(startPoint.getLat(),
					startPoint.getLon(), endPoint.getLat(), endPoint.getLon());

			// add to list view
			javax.swing.DefaultListModel listModel = new javax.swing.DefaultListModel();

			for (int i = 0; i < routes.size(); i++) {
				Route r = routes.get(i);
				listModel.addElement(r);
			}

			listView.setModel(listModel);

			mapView.addMapMarker(new MapMarkerPoint(startPoint.getLat(),
					startPoint.getLon()));
			mapView.addMapMarker(new MapMarkerPoint(endPoint.getLat(), endPoint
					.getLon()));

		} else {
			startPoint = null;
			endPoint = null;

			reloadList();
		}
	}

	private void downloadData() {
		filename = transit.downloadOsmData(Transit.Bounds_Lviv);

		transit.openData(filename);
		reloadList();
	}

	private void showRouteOnMap(Route route) {

		mapView.removeAllMapPolygons();
		mapView.removeAllMapMarkers();

		// show on map
		MapRoute polygon = new MapRoute(route);
		mapView.addMapPolygon(polygon);

//		// stops
//		String sway = route.stops;
//		String[] path = sway.substring(0, sway.length() - 1).split(";");
//
//		for (int j = 0; j < path.length; j++) {
//
//			String[] coors = path[j].split(",");
//
//			double lat = Double.parseDouble(coors[0]);
//			double lon = Double.parseDouble(coors[1]);
//
//			mapView.addMapMarker(new MapMarkerPoint(lat, lon));
//		}

		if(startPoint != null) {
			mapView.addMapMarker(new MapMarkerPoint(startPoint.getLat(), startPoint
				.getLon()));
		}
		
		if(endPoint != null) {
			mapView.addMapMarker(new MapMarkerPoint(endPoint.getLat(), endPoint
				.getLon()));
		}
	}

	private void reloadList() {

		// display all routes
		ArrayList<Route> routes = transit.getCopyOfRoutes();

		// add to list view
		javax.swing.DefaultListModel listModel = new javax.swing.DefaultListModel();

		for (int i = 0; i < routes.size(); i++) {
			Route r = routes.get(i);
			listModel.addElement(r);
		}

		listView.setModel(listModel);
	}
}
