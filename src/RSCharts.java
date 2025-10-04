import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class RSCharts {
  private static final int N = 10;        // subgroup size
  private static final double A2 = 0.308; // X-bar from R-bar
  private static final double D3 = 0.223; // R lower factor
  private static final double D4 = 1.777; // R upper factor

  private final BorderPane root = new BorderPane();
  private final TextField pathField = new TextField("put csv file name here");
  private final Button loadBtn = new Button("Load");
  private final Label status = new Label();
  private final TextArea summary = new TextArea();

  private final LineChart<Number, Number> rChart = makeChart("R Chart");
  private final LineChart<Number, Number> xChart = makeChart("X̄ Chart");

  public RSCharts() {
    // Top bar
    HBox top = new HBox(8);
    top.setPadding(new Insets(8));
    pathField.setPrefColumnCount(40);
    pathField.setOnAction(e -> loadAndPlot());
    loadBtn.setOnAction(e -> loadAndPlot());
    top.getChildren().addAll(new Label("CSV path:"), pathField, loadBtn, status);
    root.setTop(top);

    // Center area: R and X-bar charts side by side
    HBox charts = new HBox(10);
    charts.setPadding(new Insets(10));
    charts.getChildren().addAll(rChart, xChart);
    root.setCenter(charts);

    // Summary text area
    summary.setEditable(false);
    summary.setPrefRowCount(5);
    root.setBottom(summary);
  }

  public BorderPane getRoot() {
    return root;
  }

  private LineChart<Number, Number> makeChart(String title) {
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Iteration");
    NumberAxis yAxis = new NumberAxis();
    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle(title);
    chart.setLegendVisible(true);
    chart.setCreateSymbols(true);
    chart.setPrefWidth(550);
    return chart;
  }

  private void loadAndPlot() {
    try {
      File f = new File(pathField.getText().trim());
      List<double[]> groups = readCsv(f, true, N);

      double[] means = new double[groups.size()];
      double[] ranges = new double[groups.size()];

      for (int i = 0; i < groups.size(); i++) {
        double[] g = groups.get(i);
        means[i] = mean(g);
        ranges[i] = range(g);
      }

      double Rbar = mean(ranges);
      double rUCL = D4 * Rbar;
      double rLCL = D3 * Rbar;

      // --- R chart ---
      rChart.getData().clear();
      XYChart.Series<Number, Number> rData = new XYChart.Series<>();
      rData.setName("R");
      for (int i = 0; i < ranges.length; i++) {
        rData.getData().add(new XYChart.Data<>(i + 1, ranges[i]));
      }
      rChart.getData().addAll(
              rData,
              hline("UCL", rUCL, ranges.length),
              hline("CL", Rbar, ranges.length),
              hline("LCL", rLCL, ranges.length)
      );

      // Check if R chart is in control
      boolean rInControl = true;
      for (double v : ranges) {
        if (v > rUCL || v < rLCL) {
          rInControl = false;
          break;
        }
      }

      // --- X-bar chart ---
      xChart.getData().clear();
      if (rInControl) {
        double XbarBar = mean(means);
        double xUCL = XbarBar + A2 * Rbar;
        double xLCL = XbarBar - A2 * Rbar;

        XYChart.Series<Number, Number> xData = new XYChart.Series<>();
        xData.setName("X̄");
        for (int i = 0; i < means.length; i++) {
          xData.getData().add(new XYChart.Data<>(i + 1, means[i]));
        }
        xChart.setTitle("X̄ Chart (R is in control)");
        xChart.getData().addAll(
                xData,
                hline("UCL", xUCL, means.length),
                hline("CL", XbarBar, means.length),
                hline("LCL", xLCL, means.length)
        );

        summary.setText(String.format(
                "R̄ = %.4f (UCL=%.4f, CL=%.4f, LCL=%.4f) — IN CONTROL\n" +
                        "X̄̄ = %.4f (UCL=%.4f, CL=%.4f, LCL=%.4f)",
                Rbar, rUCL, Rbar, rLCL,
                XbarBar, xUCL, XbarBar, xLCL
        ));
      } else {
        xChart.setTitle("X̄ Chart (NOT SHOWN — R chart is out of control)");
        summary.setText(String.format(
                "R̄ = %.4f (UCL=%.4f, CL=%.4f, LCL=%.4f) — OUT OF CONTROL\n" +
                        "X̄ not evaluated.",
                Rbar, rUCL, Rbar, rLCL
        ));
      }

      status.setText("rows: " + groups.size());

    } catch (Exception e) {
      e.printStackTrace();
      status.setText("load error");
    }
  }

  private XYChart.Series<Number, Number> hline(String name, double y, int len) {
    XYChart.Series<Number, Number> s = new XYChart.Series<>();
    s.setName(name);
    s.getData().add(new XYChart.Data<>(1, y));
    s.getData().add(new XYChart.Data<>(len, y));
    return s;
  }

  // Simple CSV reader
  private static List<double[]> readCsv(File file, boolean hasHeader, int n) throws Exception {
    List<double[]> out = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      if (hasHeader) br.readLine();
      while ((line = br.readLine()) != null) {
        if (line.trim().isEmpty()) continue;
        String[] parts = line.split(",", -1);
        if (parts.length < n + 1) continue;
        double[] row = new double[n];
        boolean ok = true;
        for (int i = 0; i < n; i++) {
          String s = parts[i + 1].trim();
          if (s.isEmpty()) {
            ok = false;
            break;
          }
          try {
            row[i] = Double.parseDouble(s);
          } catch (NumberFormatException e) {
            ok = false;
            break;
          }
        }
        if (ok) out.add(row);
      }
    }
    if (out.isEmpty()) throw new IllegalArgumentException("no numeric rows found");
    return out;
  }

  // Helpers
  private static double mean(double[] a) {
    double s = 0;
    for (double v : a) s += v;
    return s / a.length;
  }

  private static double range(double[] a) {
    double lo = Double.POSITIVE_INFINITY, hi = Double.NEGATIVE_INFINITY;
    for (double v : a) {
      if (v < lo) lo = v;
      if (v > hi) hi = v;
    }
    return hi - lo;
  }
}
