package com.myproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.print.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class RocketFlightGraph extends JFrame {
    private List<Integer> times = new ArrayList<>();
    private List<Double> altitudes = new ArrayList<>();
    private List<Double> velocities = new ArrayList<>();
    private List<Double> accelXValues = new ArrayList<>();
    private List<Double> accelYValues = new ArrayList<>();
    private List<Double> accelZValues = new ArrayList<>();
    private List<Double> temperatures = new ArrayList<>();
    private List<Integer> events = new ArrayList<>();

    private JSlider startSlider;
    private JSlider endSlider;
    private JButton saveButton;
    private JButton loadButton;
    private ChartPanel altitudeChartPanel;
    private ChartPanel velocityChartPanel;
    private ChartPanel accelerationsChartPanel;
    private ChartPanel temperatureChartPanel;
    private JPanel controlPanel;

    public RocketFlightGraph() {
        setTitle("Rocket Flight Data Visualization");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        controlPanel = new JPanel(new BorderLayout());

        startSlider = new JSlider(0, 100, 0);
        endSlider = new JSlider(0, 100, 100);
        startSlider.addChangeListener(e -> updateCharts());
        endSlider.addChangeListener(e -> updateCharts());

        JPanel sliderPanel = new JPanel();
        sliderPanel.add(new JLabel("Start:"));
        sliderPanel.add(startSlider);
        sliderPanel.add(new JLabel("End:"));
        sliderPanel.add(endSlider);
        controlPanel.add(sliderPanel, BorderLayout.CENTER);

        saveButton = new JButton("Save Selected Data");
        loadButton = new JButton("Load New File");
        loadButton.addActionListener(e -> loadDataFromFile());
        saveButton.addActionListener(e -> saveSelectedData());

        JPanel buttonPanel = new JPanel();
        JButton printButton = new JButton("Print Graphs");
        printButton.addActionListener(e -> printGraphs());
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(printButton);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        altitudeChartPanel = new ChartPanel(createAltitudeChart());
        velocityChartPanel = new ChartPanel(createVelocityChart());
        accelerationsChartPanel = new ChartPanel(createAccelerationsChart());
        temperatureChartPanel = new ChartPanel(createTemperatureChart());

        JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, altitudeChartPanel, velocityChartPanel);
        splitPane1.setResizeWeight(0.5);
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane1, accelerationsChartPanel);
        splitPane2.setResizeWeight(0.67);
        JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane2, temperatureChartPanel);
        splitPane3.setResizeWeight(0.75);

        add(splitPane3, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        loadDataFromFile();
    }

    private File lastDirectory = new File(System.getProperty("user.dir"));

    private void loadDataFromFile() {
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            lastDirectory = fileChooser.getSelectedFile().getParentFile();
            parseJsonFile(fileChooser.getSelectedFile());
            calculateVelocities();
            updateCharts();
        }

        startSlider.setValue(0);
        endSlider.setValue(100);
    }

    private void parseJsonFile(File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            times.clear();
            altitudes.clear();
            accelXValues.clear();
            accelYValues.clear();
            accelZValues.clear();
            temperatures.clear();
            events.clear();
   
            FlightData[] flightDataArray = objectMapper.readValue(file, FlightData[].class);
            for (FlightData data : flightDataArray) {
                times.add(data.getTimestamp());
                altitudes.add(data.getAltitude());
                accelXValues.add(data.getAccelX());
                accelYValues.add(data.getAccelY());
                accelZValues.add(data.getAccelZ());
                temperatures.add(data.getTemperature());
                events.add(data.getEvent());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading JSON file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateVelocities() {
        velocities.clear();
        int windowSize = 16; // Number of samples to smooth over
        for (int i = 0; i < times.size(); i++) {
            double smoothedVelocity = 0.0;
            int count = 0;
            for (int j = Math.max(0, i - windowSize / 2); j <= Math.min(times.size() - 1, i + windowSize / 2); j++) {
                double deltaTime = (j > 0) ? (times.get(j) - times.get(j - 1)) / 1000.0 : 1;
                if (deltaTime > 0) {
                    smoothedVelocity += (altitudes.get(j) - (j > 0 ? altitudes.get(j - 1) : altitudes.get(j))) / deltaTime;
                    count++;
                }
            }
            velocities.add(count > 0 ? smoothedVelocity / count : 0.0);
        }
    }

    private void saveSelectedData() {
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            lastDirectory = fileChooser.getSelectedFile().getParentFile();
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }

            List<FlightData> selectedData = new ArrayList<>();
            double startPercent = startSlider.getValue();
            double endPercent = endSlider.getValue();

            int lowerIndex = Math.max(0, (int) ((startPercent / 100.0) * (times.size() - 1)));
            int upperIndex = Math.max(lowerIndex, (int) ((endPercent / 100.0) * (times.size() - 1)));

            for (int i = lowerIndex; i <= upperIndex; i++) {
                selectedData.add(new FlightData(times.get(i), altitudes.get(i), accelXValues.get(i), accelYValues.get(i), accelZValues.get(i), temperatures.get(i), events.get(i)));
            }

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, selectedData);
                JOptionPane.showMessageDialog(this, "Data saved successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JFreeChart createAltitudeChart() {
        double maxAltitude = altitudes.stream().mapToDouble(v -> v).max().orElse(0);
        XYSeries series = new XYSeries("Altitude vs Time");
        for (int i = 0; i < times.size(); i++) {
            series.add(times.get(i), altitudes.get(i));
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Altitude vs Time (Max: " + String.format("%.2f", maxAltitude) + " ft)",
                "Time (ms)", "Altitude (ft)", dataset
        );
        
        XYPlot plot = chart.getXYPlot();
        for (int i = 0; i < times.size(); i++) {
            if (events.get(i) != 0) {
                Color eventColor;
                String eventLabel;
                switch (events.get(i)) {
                    case 2: eventColor = Color.ORANGE; eventLabel = "Apogee"; break;
                    case 13: eventColor = Color.GREEN; eventLabel = "Launch"; break;
                    case 15: eventColor = Color.BLUE; eventLabel = "Burnout"; break;
                    case 17: eventColor = Color.MAGENTA; eventLabel = "Landing"; break;
                    default: eventColor = Color.RED; eventLabel = "Event " + events.get(i); break;
                }
                
                ValueMarker marker = new ValueMarker(times.get(i));
                marker.setPaint(eventColor);
                marker.setStroke(new BasicStroke(1.5f));
                marker.setLabel(eventLabel);
                marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                plot.addDomainMarker(marker);
            }
        }
        
        return chart;
    }

    private JFreeChart createVelocityChart() {
        double maxVelocity = velocities.stream().mapToDouble(v -> v).max().orElse(0);
        XYSeries series = new XYSeries("Velocity vs Time");
        for (int i = 0; i < times.size(); i++) {
            series.add(times.get(i), velocities.get(i));
        }
        return ChartFactory.createXYLineChart(
                "Velocity vs Time (Max: " + String.format("%.2f", maxVelocity) + " ft/s)",
                "Time (ms)", "Velocity (ft/s)", new XYSeriesCollection(series)
        );
    }

    private JFreeChart createAccelerationsChart() {
        double maxAccelX = accelXValues.stream().mapToDouble(v -> v).max().orElse(0);
        double maxAccelY = accelYValues.stream().mapToDouble(v -> v).max().orElse(0);
        double maxAccelZ = accelZValues.stream().mapToDouble(v -> v).max().orElse(0);
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries accelerationSeries = new XYSeries("Acceleration Y vs Time");
        for (int i = 0; i < times.size(); i++) {
            accelerationSeries.add(times.get(i), accelYValues.get(i));
        }
        dataset.addSeries(accelerationSeries);
        return ChartFactory.createXYLineChart(
                "Acceleration vs Time (Max X: " + String.format("%.2f", maxAccelX) + " G, Max Y: " + String.format("%.2f", maxAccelY) + " G, Max Z: " + String.format("%.2f", maxAccelZ) + " G)",
                "Time (ms)", "Acceleration (G)", dataset);
    }

    private JFreeChart createTemperatureChart() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries temperatureSeries = new XYSeries("Temperature vs Time");
        for (int i = 0; i < times.size(); i++) {
            temperatureSeries.add(times.get(i), temperatures.get(i));
        }
        dataset.addSeries(temperatureSeries);
        return ChartFactory.createXYLineChart("Temperature vs Time", "Time (ms)", "Temperature (C)", dataset);
    }

      private void updateCharts() {
        double startPercent = startSlider.getValue();
        double endPercent = endSlider.getValue();

        int lowerIndex = Math.max(0, (int) ((startPercent / 100.0) * (times.size() - 1)));
        int upperIndex = Math.max(lowerIndex, (int) ((endPercent / 100.0) * (times.size() - 1)));

        XYSeries altitudeSeries = new XYSeries("Altitude vs Time");
        for (int i = lowerIndex; i <= upperIndex; i++) {
            altitudeSeries.add(times.get(i), altitudes.get(i));
        }
        double maxAltitude = altitudes.stream().mapToDouble(v -> v).max().orElse(0);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Altitude vs Time (Max: " + String.format("%.2f", maxAltitude) + " ft)",
                "Time (ms)",
                "Altitude (ft)",
                new XYSeriesCollection(altitudeSeries));
        altitudeChartPanel.setChart(chart);
        
        // Add event markers
        XYPlot plot = chart.getXYPlot();
        for (int i = 0; i < times.size(); i++) {
            if (events.get(i) != 0) {
                Color eventColor;
                String eventLabel;
                switch (events.get(i)) {
                    case 2: eventColor = Color.ORANGE; eventLabel = "Apogee"; break;
                    case 13: eventColor = Color.GREEN; eventLabel = "Launch"; break;
                    case 15: eventColor = Color.BLUE; eventLabel = "Burnout"; break;
                    case 17: eventColor = Color.MAGENTA; eventLabel = "Landing"; break;
                    default: eventColor = Color.RED; eventLabel = "Event " + events.get(i); break;
                }
                
                ValueMarker marker = new ValueMarker(times.get(i));
                marker.setPaint(eventColor);
                marker.setStroke(new BasicStroke(1.5f));
                marker.setLabel(eventLabel);
                marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                plot.addDomainMarker(marker);
            }
        }

        XYSeries velocitySeries = new XYSeries("Velocity vs Time");
        for (int i = lowerIndex; i <= upperIndex; i++) {
            velocitySeries.add(times.get(i), velocities.get(i));
        }
        double maxVelocity = velocities.stream().mapToDouble(v -> v).max().orElse(0);
        velocityChartPanel.setChart(ChartFactory.createXYLineChart(
                "Velocity vs Time (Max: " + String.format("%.2f", maxVelocity) + " ft/s)", "Time (ms)", "Velocity (ft/s)", new XYSeriesCollection(velocitySeries)));

        XYSeries accelXSeries = new XYSeries("X Acceleration vs Time");
        for (int i = lowerIndex; i <= upperIndex; i++) {
            accelXSeries.add(times.get(i), accelXValues.get(i));
        }

        XYSeries accelYSeries = new XYSeries("Y Acceleration vs Time");
        for (int i = lowerIndex; i <= upperIndex; i++) {
            accelYSeries.add(times.get(i), accelYValues.get(i));
        }

        XYSeries accelZSeries = new XYSeries("Z Acceleration vs Time");
        for (int i = lowerIndex; i <= upperIndex; i++) {
            accelZSeries.add(times.get(i), accelZValues.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(accelXSeries);
        dataset.addSeries(accelYSeries);
        dataset.addSeries(accelZSeries);
        
        double maxAccelX = accelXValues.stream().mapToDouble(v -> v).max().orElse(0);
        double maxAccelY = accelYValues.stream().mapToDouble(v -> v).max().orElse(0);
        double maxAccelZ = accelZValues.stream().mapToDouble(v -> v).max().orElse(0);
        accelerationsChartPanel.setChart(ChartFactory.createXYLineChart(
                "Acceleration vs Time (Max X: " + String.format("%.2f", maxAccelX) + " G, Max Y: " + String.format("%.2f", maxAccelY) + " G, Max Z: " + String.format("%.2f", maxAccelZ) + " G)",
                "Time (ms)", "Acceleration (G)", dataset));

        XYSeries temperatureSeries = new XYSeries("Temperature vs Time");
        for (int i = lowerIndex; i <= upperIndex; i++) {
            temperatureSeries.add(times.get(i), temperatures.get(i));
        }
        temperatureChartPanel.setChart(ChartFactory.createXYLineChart(
                "Temperature vs Time", "Time (ms)", "Temperature (C)", new XYSeriesCollection(temperatureSeries)));
    }

    private void printGraphs() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Print Graphs");
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double scaleX = pageFormat.getImageableWidth() / getContentPane().getWidth();
            double scaleY = pageFormat.getImageableHeight() / getContentPane().getHeight();
            double scaleFactor = Math.min(scaleX, scaleY);
            g2d.scale(scaleFactor, scaleFactor);
            getContentPane().printAll(graphics);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RocketFlightGraph::new);
    }
}
