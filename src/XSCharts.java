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

public class XSCharts {
  private static final int N = 10;      // subgroup size (10 values per row)

  // SPC constants for X-bar/S when n = 10
  private static final double A3 = 0.975;  // X-bar limits from S-bar
  private static final double B3 = 0.284;  // S LCL factor
  private static final double B4 = 1.716;  // S UCL factor

  private final BorderPane root = new BorderPane();
  private final TextField pathField = new TextField("put csv file name here");
  private final Button loadBtn = new Button("Load");
  private final Label status = new Label();
  private final TextArea summary = new TextArea();

  private final LineChart<Number, Number> sChart = makeChart("S Chart");
  private final LineChart<Number, Number> xChart = makeChart("X̄ Chart (from S)");

  public XSCharts() {
    // Top: path + load
    HBox top = new HBox(8);
    top.setPadding(new Insets(8));
    pathField.setPrefColumnCount(45);
    pathField.setOnAction(e -> loadAndPlot());
    loadBtn.setOnAction(e -> loadAndPlot());
    top.getChildren().addAll(new Label("CSV path:"), pathField, loadBtn, status);
    root.setTop(top);

    // Center: S (left) and X-bar (right)
    HBox charts = new HBox(10);
    charts.setPadding(new Insets(10));
    charts.getChildren().addAll(sChart, xChart);
    root.setCenter(charts);

    summary.setEditable(false);
    summary.setPrefRowCount(5);
    root.setBottom(summary);

    sChart.setPrefWidth(550);
    xChart.setPrefWidth(550);
  }

  public BorderPane getRoot() { return root; }

  private LineChart<Number, Number> makeChart(String title) {
    NumberAxis x = new NumberAxis(); x.setLabel("Iteration");
    NumberAxis y = new NumberAxis();
    LineChart<Number, Number> c = new LineChart<>(x, y);
    c.setTitle(title);
    c.setLegendVisible(true);
    c.setCreateSymbols(true);
    return c;
  }

  private XYChart.Series<Number, Number> hline(String name, double y, int len) {
    XYChart.Series<Number, Number> s = new XYChart.Series<>();
    s.setName(name);
    s.getData().add(new XYChart.Data<>(1, y));
    s.getData().add(new XYChart.Data<>(len, y));
    return s;
  }

  private void loadAndPlot() {
    try {
      File f = new File(pathField.getText().trim());
      List<double[]> groups = readCsv(f, true, N);

      double[] means = new double[groups.size()];
      double[] svals = new double[groups.size()];
      for (int i = 0; i < groups.size(); i++) {
        double[] g = groups.get(i);
        means[i] = mean(g);
        svals[i] = sdev(g);            // sample SD per subgroup
      }

      double Sbar = mean(svals);
      double sUCL = B4 * Sbar;
      double sLCL = B3 * Sbar;

      // --- S chart first ---
      sChart.getData().clear();
      XYChart.Series<Number, Number> sData = new XYChart.Series<>();
      sData.setName("S");
      for (int i = 0; i < svals.length; i++) sData.getData().add(new XYChart.Data<>(i + 1, svals[i]));
      sChart.getData().addAll(
              sData,
              hline("UCL", sUCL, svals.length),
              hline("CL",  Sbar, svals.length),
              hline("LCL", sLCL, svals.length)
      );

      // check control on S
      boolean sInControl = true;
      for (double v : svals) { if (v < sLCL || v > sUCL) { sInControl = false; break; } }

      // --- X-bar from S (only if S is in control) ---
      xChart.getData().clear();
      double xbarBar = mean(means);
      double xUCL = xbarBar + A3 * Sbar;
      double xLCL = xbarBar - A3 * Sbar;

      if (sInControl) {
        XYChart.Series<Number, Number> xData = new XYChart.Series<>();
        xData.setName("X̄");
        for (int i = 0; i < means.length; i++) xData.getData().add(new XYChart.Data<>(i + 1, means[i]));
        xChart.setTitle("X̄ Chart (S is in control)");
        xChart.getData().addAll(
                xData,
                hline("UCL", xUCL, means.length),
                hline("CL",  xbarBar, means.length),
                hline("LCL", xLCL, means.length)
        );
        summary.setText(String.format(
                "S̄ = %.4f (UCL=%.4f, CL=%.4f, LCL=%.4f) — IN CONTROL%n" +
                        "X̄̄ = %.4f (UCL=%.4f, CL=%.4f, LCL=%.4f)",
                Sbar, sUCL, Sbar, sLCL,
                xbarBar, xUCL, xbarBar, xLCL
        ));
      } else {
        xChart.setTitle("X̄ Chart (NOT SHOWN — S chart is out of control)");
        summary.setText(String.format(
                "S̄ = %.4f (UCL=%.4f, CL=%.4f, LCL=%.4f) — OUT OF CONTROL%n" +
                        "X̄ not evaluated.",
                Sbar, sUCL, Sbar, sLCL
        ));
      }

      status.setText("rows: " + groups.size());
    } catch (Exception e) {
      e.printStackTrace();
      status.setText("load error");
    }
  }

  // CSV reader: header row, first col ignored, next N numeric cells
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
          if (s.isEmpty()) { ok = false; break; }
          try { row[i] = Double.parseDouble(s); } catch (NumberFormatException ex) { ok = false; break; }
        }
        if (ok) out.add(row);
      }
    }
    if (out.isEmpty()) throw new IllegalArgumentException("no numeric rows found");
    return out;
  }

  // tiny math
  private static double mean(double[] a) { double s=0; for (double v: a) s += v; return s / a.length; }
  private static double sdev(double[] a) {
    double m = mean(a), ss = 0; for (double v: a) ss += (v - m) * (v - m);
    return Math.sqrt(ss / (a.length - 1)); // sample SD
  }
}
