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
import java.util.Map;

public class RSCharts {
  // subgroup size (columns B..K = 10 numbers)
  private static final int N = 10;

  // SPC constants for n=10
  private static final Map<Integer, Double> D3 = Map.of(10, 0.223);
  private static final Map<Integer, Double> D4 = Map.of(10, 1.777);
  private static final Map<Integer, Double> B3 = Map.of(10, 0.284);
  private static final Map<Integer, Double> B4 = Map.of(10, 1.716);

  private final BorderPane root = new BorderPane();
  private final TextField pathField = new TextField("XbarData.csv");
  private final Button loadBtn = new Button("Load");
  private final Label status = new Label();
  private final TextArea summary = new TextArea();

  private LineChart<Number, Number> rChart;
  private LineChart<Number, Number> sChart;

  public RSCharts() {
    // top bar: path + load
    HBox top = new HBox(8);
    top.setPadding(new Insets(8));
    pathField.setPrefColumnCount(40);
    pathField.setOnAction(e -> loadAndPlot());
    loadBtn.setOnAction(e -> loadAndPlot());
    top.getChildren().addAll(new Label("CSV path:"), pathField, loadBtn, status);
    root.setTop(top);

    // two charts side by side
    rChart = makeChart("R Chart");
    sChart = makeChart("S Chart");
    HBox charts = new HBox(12);
    charts.setPadding(new Insets(8));
    charts.getChildren().addAll(rChart, sChart);
    root.setCenter(charts);

    // small summary box
    summary.setEditable(false);
    summary.setPrefRowCount(4);
    root.setBottom(summary);
  }

  public BorderPane getRoot() { return root; }

  // minimal chart factory
  private LineChart<Number, Number> makeChart(String title) {
    NumberAxis x = new NumberAxis(); x.setLabel("Iteration");
    NumberAxis y = new NumberAxis();
    LineChart<Number, Number> c = new LineChart<>(x, y);
    c.setTitle(title);
    c.setLegendVisible(true);
    c.setCreateSymbols(true);
    c.setPrefWidth(530);
    return c;
  }

  // load CSV, compute, and plot
  private void loadAndPlot() {
    try {
      File f = new File(pathField.getText().trim());
      List<double[]> groups = readCsvRowsSkipFirstColExpectN(f, /*header*/true, N);

      // per-row R and S
      double[] R = new double[groups.size()];
      double[] S = new double[groups.size()];
      for (int i = 0; i < groups.size(); i++) {
        double[] g = groups.get(i);
        R[i] = range(g);
        S[i] = sdev(g);
      }
      double Rbar = mean(R);
      double Sbar = mean(S);

      // limits
      double rLCL = D3.get(N) * Rbar;
      double rUCL = D4.get(N) * Rbar;
      double sLCL = B3.get(N) * Sbar;
      double sUCL = B4.get(N) * Sbar;

      // plot R
      rChart.getData().clear();
      XYChart.Series<Number, Number> rData = new XYChart.Series<>();
      rData.setName("R");
      for (int i = 0; i < R.length; i++) rData.getData().add(new XYChart.Data<>(i + 1, R[i]));
      rChart.getData().addAll(rData, hline("UCL", rUCL, R.length),
              hline("CL",  Rbar, R.length),
              hline("LCL", rLCL, R.length));

      // plot S
      sChart.getData().clear();
      XYChart.Series<Number, Number> sData = new XYChart.Series<>();
      sData.setName("S");
      for (int i = 0; i < S.length; i++) sData.getData().add(new XYChart.Data<>(i + 1, S[i]));
      sChart.getData().addAll(sData, hline("UCL", sUCL, S.length),
              hline("CL",  Sbar, S.length),
              hline("LCL", sLCL, S.length));

      summary.setText(String.format(
              "R̄ = %.4f  (UCL=%.4f, CL=%.4f, LCL=%.4f)\nS̄ = %.4f  (UCL=%.4f, CL=%.4f, LCL=%.4f)",
              Rbar, rUCL, Rbar, rLCL, Sbar, sUCL, Sbar, sLCL
      ));
      status.setText("rows: " + groups.size());
    } catch (Exception ex) {
      ex.printStackTrace();
      status.setText("load error");
    }
  }

  // horizontal line series
  private XYChart.Series<Number, Number> hline(String name, double y, int len) {
    XYChart.Series<Number, Number> s = new XYChart.Series<>();
    s.setName(name);
    s.getData().add(new XYChart.Data<>(1, y));
    s.getData().add(new XYChart.Data<>(len, y));
    return s;
  }

  // CSV reader
  // Expected:
  //   Row 1 = headers
  //   Col A = label (ignored)
  //   Cols B..K = 10 numeric values (n=10)
  private static List<double[]> readCsvRowsSkipFirstColExpectN(File file, boolean hasHeader, int n) throws Exception {
    List<double[]> out = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      if (hasHeader) br.readLine(); // drop header
      while ((line = br.readLine()) != null) {
        if (line.trim().isEmpty()) continue;
        String[] parts = line.split(",", -1);
        if (parts.length < n + 1) continue;   // need label + n values
        double[] row = new double[n];
        boolean ok = true;
        for (int i = 0; i < n; i++) {
          String s = parts[i + 1].trim(); // skip first col
          if (s.isEmpty()) { ok = false; break; }
          try { row[i] = Double.parseDouble(s); }
          catch (NumberFormatException e) { ok = false; break; }
        }
        if (ok) out.add(row);
      }
    }
    if (out.isEmpty()) throw new IllegalArgumentException("no numeric rows found");
    return out;
  }

  // tiny math helpers
  private static double mean(double[] a) { double s=0; for(double v:a) s+=v; return s/a.length; }
  private static double range(double[] a) {
    double lo=Double.POSITIVE_INFINITY, hi=Double.NEGATIVE_INFINITY;
    for(double v:a){ if(v<lo) lo=v; if(v>hi) hi=v; }
    return hi-lo;
  }
  private static double sdev(double[] a) {
    double m=mean(a), ss=0;
    for(double v:a) ss += (v-m)*(v-m);
    return Math.sqrt(ss/(a.length-1)); // sample SD
  }
}
