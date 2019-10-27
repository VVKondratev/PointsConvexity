package ru.vvk.convex;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartScene;
import org.jzy3d.chart.controllers.mouse.camera.NewtCameraMouseController;
import org.jzy3d.chart.factories.NewtChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Main {
	/**
	 *
	 */
	private JFrame frame;
	private Chart chart;

	private Surface surface;

	// private Surface sMain;
	// private Surface sFigure;
	private JTextField tbError;

	private JTextArea taC;
	private JTextArea txMain;
	private JTextArea txFuncs;
	private JPanel pnChart;
	private JCheckBox cbScatter;
	private JPanel pnColor;
	private JPanel pnBoundColor;
	private JTextArea txSummary;
	private JLabel lbTime;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					Main window = new Main();
					window.frame.setLocationRelativeTo(null);
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
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 812, 671);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.1);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel.add(splitPane);

		JPanel panel_1 = new JPanel();
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_2.setPreferredSize(new Dimension(200, 10));
		panel_2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		panel_2.setMinimumSize(new Dimension(100, 10));
		panel_1.add(panel_2, BorderLayout.WEST);
		panel_2.setLayout(null);

		tbError = new JTextField();
		tbError.setBounds(113, 68, 32, 20);
		panel_2.add(tbError);
		tbError.setText("0.05");
		tbError.setColumns(10);

		JLabel label_1 = new JLabel("Погрешность");
		label_1.setBounds(7, 71, 67, 14);
		panel_2.add(label_1);

		JLabel label_2 = new JLabel("c={x;y;z}");
		label_2.setBounds(10, 14, 54, 14);
		panel_2.add(label_2);

		JButton button = new JButton("Расчет");
		button.setBounds(109, 99, 81, 23);
		panel_2.add(button);

		taC = new JTextArea();
		taC.setBounds(103, 14, 91, 42);
		panel_2.add(taC);
		taC.setText("0.0;0.0;0.0");

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));

		txFuncs = new JTextArea();
		// txFuncs.setText("Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z - 1,2) - 1");
		txFuncs.setText("Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z - 1.95,2) - 1");
		txFuncs.setFont(new Font("Monospaced", Font.BOLD, 12));
		panel_3.add(txFuncs);

		txMain = new JTextArea();
		txMain.setFont(new Font("Monospaced", Font.BOLD, 12));
		// txMain.setText("Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z,2) - 1");
		txMain.setText("-Math.pow(x, 2) - Math.pow(y, 2) - Math.pow(z,2) + 1");
		txMain.setBorder(new LineBorder(new java.awt.Color(0, 0, 0)));
		panel_3.add(txMain, BorderLayout.NORTH);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				calculate();
			}
		});

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setOneTouchExpandable(true);
		splitPane.setRightComponent(splitPane_1);

		pnChart = new JPanel();
		splitPane_1.setRightComponent(pnChart);
		pnChart.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_4.setPreferredSize(new Dimension(250, 10));
		splitPane_1.setLeftComponent(panel_4);

		cbScatter = new JCheckBox("Scatter");
		cbScatter.setBounds(5, 5, 61, 23);
		cbScatter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				buildChart();
			}
		});
		panel_4.setLayout(null);
		panel_4.add(cbScatter);

		pnColor = new JPanel();
		pnColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				java.awt.Color currentBackground = pnColor.getBackground();
				java.awt.Color color = JColorChooser.showDialog(pnColor, "Выбор цвета поверхности", currentBackground);
				if ((color != null) && (currentBackground != color)) {
					pnColor.setBackground(color);
					buildChart();
				}
			}
		});
		pnColor.setBackground(java.awt.Color.RED);
		pnColor.setBounds(113, 34, 51, 15);
		panel_4.add(pnColor);

		JLabel lblNewLabel = new JLabel("Цвет поверхности");
		lblNewLabel.setBounds(10, 35, 104, 14);
		panel_4.add(lblNewLabel);

		JLabel label = new JLabel("Цвет границы");
		label.setBounds(10, 60, 93, 14);
		panel_4.add(label);

		pnBoundColor = new JPanel();
		pnBoundColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				java.awt.Color currentBackground = pnBoundColor.getBackground();
				java.awt.Color color = JColorChooser.showDialog(pnBoundColor, "Выбор цвета границы", currentBackground);
				if ((color != null) && (currentBackground != color)) {
					pnBoundColor.setBackground(color);
					buildChart();
				}
			}
		});
		pnBoundColor.setBackground(java.awt.Color.BLUE);
		pnBoundColor.setBounds(113, 60, 51, 15);
		panel_4.add(pnBoundColor);

		txSummary = new JTextArea();
		txSummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txSummary.setBackground(SystemColor.control);
		txSummary.setEditable(false);
		txSummary.setBounds(10, 110, 327, 261);
		panel_4.add(txSummary);

		JLabel label_3 = new JLabel("Время расчета:");
		label_3.setBounds(10, 85, 93, 14);
		panel_4.add(label_3);

		lbTime = new JLabel("");
		lbTime.setBounds(113, 85, 127, 14);
		panel_4.add(lbTime);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("Menu");
		menuBar.add(mnNewMenu);

		JMenu options = new JMenu("Options");
		menuBar.add(options);

		JButton btnCountpresetsets = new JButton("CountPresetSets");
		btnCountpresetsets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculateSetOfSets();
			}
		});
		options.add(btnCountpresetsets);
	}

	private void buildChart() {
		pnChart.setVisible(false);
		pnChart.removeAll();

		chart = NewtChartComponentFactory.chart(Quality.Intermediate);
		@SuppressWarnings("unused")
		ChartScene scene = chart.getScene();
		@SuppressWarnings("unused")
		NewtCameraMouseController controller = new NewtCameraMouseController(chart);
		Component canvas = (Component) chart.getCanvas();
		pnChart.add(canvas, BorderLayout.CENTER);

		// Очистка экрана
		for (AbstractDrawable draw : chart.getScene().getGraph().getAll()) {
			chart.getScene().getGraph().remove(draw);
		}

		java.awt.Color colorAwt = pnColor.getBackground();
		Color color = new Color(colorAwt.getRed(), colorAwt.getGreen(), colorAwt.getBlue(), colorAwt.getAlpha());

		AbstractDrawable build = null;
		if (cbScatter.isSelected()) {
			build = Draw.get(surface.getInners(), color);
		} else {
			build = Draw.getShape(surface.getInnerPolygons(), color);
		}

		chart.getScene().getGraph().add(build);

		colorAwt = pnBoundColor.getBackground();
		color = new Color(colorAwt.getRed(), colorAwt.getGreen(), colorAwt.getBlue(), colorAwt.getAlpha());

		if (cbScatter.isSelected()) {
			build = Draw.get(surface.getOuters(), color);
		} else {
			build = Draw.getShape(surface.getOutersPolygons(), color);
		}

		chart.getScene().getGraph().add(build);

		for (AbstractDrawable graph : surface.getExt()) {
			chart.getScene().getGraph().add(graph);
		}
		pnChart.setVisible(true);
	}

	private void calculate() {
		try {
			String[] s = new String[5];
			s[0] = "Мера АxA";
			s[1] = "Мера A относительно двух выпуклостей";
			s[2] = "Мера A1 относительно первой выпуклости";
			s[3] = "Мера A2 относительно второй выпуклости";
			s[4] = "Мера выпуклости";
			OuterTable window = new OuterTable(s);
			double[][] pointC;

			try {
				if (taC.getText().trim().length() > 0) {
					String[] line = taC.getText().split("\n");
					pointC = new double[line.length][3];
					// �������������� ������ �� ���� �������-������
					for (int i = 0; i < line.length; i++) {
						final String str = line[i].trim();
						if (!str.isEmpty() && !str.startsWith("//")) {
							String[] kekeke = str.split(";");
							if (kekeke.length == 3) {
								for (int j = 0; j < 3; j++) {
									pointC[i][j] = Double.parseDouble(kekeke[j]);
								}
							}
						}
					}
				} else {
					// ��������
					pointC = new double[0][3];
				}
			} catch (Exception e) {
				Utils.error(e.getMessage());
				// ��������
				pointC = new double[0][3];
			}

			Instant start = Instant.now();

			Grid g = new Grid(new Point(-2d, -2d, -2d), new Point(4d, 4d, 4d), Utils.convert(tbError, "error"));

			IFunction3d fMain = Utils.getFunction(txMain.getText().trim());
			surface = new Surface(g, fMain);

			for (String line : txFuncs.getText().split("\n")) {
				final String str = line.trim();
				if (!str.isEmpty() && !str.startsWith("//")) {
					final IFunction3d figure = Utils.getFunction(str);
					final Surface sFigure = new Surface(g, figure);
					surface = surface.innerOf(sFigure);
				}
			}

			// surface.convex(pointC);
			surface.fractioconvex(pointC);
			txSummary.setText(surface.getSummary());
			s = surface.getData();
			window.addRaw(s);

			buildChart();
			Instant end = Instant.now();
			Duration diff = Duration.between(start, end);

			lbTime.setText(
					String.format("%d:%02d:%02d", diff.toHours() % 60, diff.toMinutes() % 60, diff.getSeconds() % 60));

		} catch (Exception e) {
			e.printStackTrace();
			Utils.error(e.getMessage());
		}
	}

	private void calculateSetOfSets() {

		final int av = 2;
		final int bv = 2;
		final int cv = 2;

		String[] s = new String[8];
		s[0] = "Мера АxA";
		s[1] = "Мера A относительно двух выпуклостей";
		s[2] = "Мера A1 относительно первой выпуклости";
		s[3] = "Мера A2 относительно второй выпуклости";
		s[4] = "Мера выпуклости";
		s[5] = "a";
		s[6] = "b";
		s[7] = "c";

		String[][] s1 = new String[1][3];
		String[][] s2 = new String[1][8];

		double[][] pointC = new double[2][3];
		pointC[0][0] = 0;
		pointC[0][1] = -0.1;
		pointC[0][2] = 0.9;
		pointC[1][0] = 0;
		pointC[1][0] = 0.1;
		pointC[1][0] = 0.1;

		double[] a = new double[3];
		a[0] = 0.0;
		a[2] = pointC[0][2];
		double[] b = new double[3];

		double[] c = new double[3];
		
		OuterTable window = new OuterTable(s);
		
		

		final int n = 5;
		for(int i = 1;i < n;i++) {
			a[2] = pointC[0][2]+i*(1-pointC[0][2])/n;
			a[1]=Math.sqrt(1-a[2]*a[2]);
			for(double y = 1/(double)n; y < 1;y+=1/(double)n) {
				b[1] = y;
				c[1] = y;
				for(int j = 0;j < n;j++) {
					b[2] = pointC[0][2]+j*(Math.sqrt(1-y*y)-pointC[0][2])/n;
					b[0] = -Math.sqrt(1-y*y-b[2]*b[2]);
					for(int k =0;k < n;k++) {
						c[2] = pointC[0][2]+k*(Math.sqrt(1-y*y)-pointC[0][2])/n;
						c[0] = Math.sqrt(1-y*y-c[2]*c[2]);
						s1[0] = getMeasureOfConvex(a, b, c, pointC);
						for(int l = 0;l < s1[0].length;l++) {
							s2[0][l]=s1[0][l];
						}
						s2[0][s1[0].length]=(String.format("%.3f",a[0])+";"+String.format("%.3f",a[1])+";"+String.format("%.3f",a[2]));
						s2[0][s1[0].length+1]=(String.format("%.3f",b[0])+";"+String.format("%.3f",b[1])+";"+String.format("%.3f",b[2]));
						s2[0][s1[0].length+2]=(String.format("%.3f",c[0])+";"+String.format("%.3f",c[1])+";"+String.format("%.3f",c[2]));
						window.addRaw(s2[0]);
					}
							
				}
				
				//System.out.println(y);
			}
				
		}
	}

	private String[] getMeasureOfConvex(double[] a, double[] b, double[] c, double[][] pointC) {
		String txMain = "-Math.pow(x, 2) - Math.pow(y, 2) - Math.pow(z,2) + 1";

		Instant start = Instant.now();
		try {
			Grid g = new Grid(new Point(-2d, -2d, -2d), new Point(4d, 4d, 4d), Utils.convert(tbError, "error"));

			IFunction3d fMain = Utils.getFunction(txMain);
			surface = new Surface(g, fMain);

			final IFunction3d figure = Utils.getFunction(getFunction(a, b, pointC[0]));
			final Surface sFigure = new Surface(g, figure);
			surface = surface.innerOf(sFigure);

			final IFunction3d figure1 = Utils.getFunction(getFunction(a, c, pointC[0]));
			final Surface sFigure1 = new Surface(g, figure1);
			surface = surface.innerOf(sFigure1);

			final IFunction3d figure2 = Utils.getFunction(getFunction(b, c, pointC[1]));
			final Surface sFigure2 = new Surface(g, figure2);
			surface = surface.innerOf(sFigure2);

			// surface.convex(pointC);
			surface.fractioconvex(pointC);
			txSummary.setText(surface.getSummary());

			//buildChart();
		} catch (Exception e) {

		}
		Instant end = Instant.now();
		Duration diff = Duration.between(start, end);

		lbTime.setText(
				String.format("%d:%02d:%02d", diff.toHours() % 60, diff.toMinutes() % 60, diff.getSeconds() % 60));

		return surface.getData();
	}

	private String getFunction(double[] x, double[] y, double[] c) {
		double x3 = c[0], y3 = c[1], z3 = c[2];
		double x2 = y[0], y2 = y[1], z2 = y[2];
		double x1 = x[0], y1 = x[1], z1 = x[2];
		// get function text
		final double A = ((y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1));
		final double B = ((x3 - x1) * (z2 - z1) - (x2 - x1) * (z3 - z1));
		final double C = ((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1));
		final double D = -1.0 * (A * x1 + B * y1 + C * z1);

		return (A + "*x+" + B + "*y+" + C + "*z+" + D);
	}
}
