package com.bossly.osm.transit.test;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.xml.stream.Location;

import com.bossly.osm.transit.Route;

public class Application {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Application window = new Application();
					window.frame.setVisible(true);
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

	Transit transit = new Transit();
	String filename = "temp.osm";
	JList listResult;
	ArrayList<Route> result;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);

		JButton btnDownload = new JButton("Download");
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				filename = transit.downloadOsmData(Transit.Bounds_Lviv);
			}
		});
		panel.add(btnDownload);

		JButton btnLoadFromFile = new JButton("Load from file");
		btnLoadFromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transit.openData(filename);
			}
		});
		panel.add(btnLoadFromFile);

		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));

		JLabel lblFrom = new JLabel("From");
		panel_1.add(lblFrom);

		textField = new JTextField();
		panel_1.add(textField);
		textField.setColumns(10);

		JLabel lblNewLabel = new JLabel("To");
		panel_1.add(lblNewLabel);

		textField_1 = new JTextField();
		panel_1.add(textField_1);
		textField_1.setColumns(10);

		JButton btnFind = new JButton("Find");
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String from = textField.getText();
				String to = textField_1.getText();
				
				ArrayList<Route> routes = transit.findRoutes(from + "," + to);
				result = routes;

				// add to list view

				// TODO add your handling code here:
				javax.swing.DefaultListModel listModel = new javax.swing.DefaultListModel();

				for (int i = 0; i < routes.size(); i++) {
					Route r = routes.get(i);
					listModel.addElement(r.name);
				}

				listResult.setModel(listModel);
			}
		});
		panel_1.add(btnFind);

		JList list = new JList();
		list.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());
					
					Route r = result.get(index);
					showRoute(r);

				} else if (evt.getClickCount() == 3) { // Triple-click
					int index = list.locationToIndex(evt.getPoint());

				}
			}
		});
		
		listResult = list;
		frame.getContentPane().add(list, BorderLayout.CENTER);
	}
	
	public void showRoute(Route route)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("http://maps.googleapis.com/maps/api/staticmap");
		builder.append("?size=" + 400 + "x" + 500);
		builder.append("&path=color:0x0000ff" +
				URLEncoder.encode("|weight:5"));
		String sway = route.genPath();

		String[] path = sway.substring(0, sway.length() - 1).split(";");

		for (int j = 0; j < path.length; j++) {
			String[] coors = path[j].split(",");

			double lat = Double.parseDouble(coors[0]);
			double lon = Double.parseDouble(coors[1]);

			builder.append(URLEncoder.encode("|" + lat + "," + lon));
		}

		builder.append("&sensor=true");
		
		try {
			openWebpage(new URI(builder.toString()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
