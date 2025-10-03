/*
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

public class X_R_GUI extends Application {
  private double[][] averages;

  public void start(Stage primaryStage) {
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Point");
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Values");

    NumberAxis xAxis2 = new NumberAxis();
    xAxis2.setLabel("Point");
    NumberAxis yAxis2 = new NumberAxis();
    yAxis2.setLabel("Values");

    //Set up the actual chart
    LineChart<Number, Number> rangeChart = new LineChart<>(xAxis, yAxis);
    rangeChart.setPrefSize(600,800);
    LineChart<Number, Number> averageChart = new LineChart<>(xAxis2, yAxis2);
    averageChart.setPrefSize(600,800);
    averageChart.setTitle("Average Chart");
    averageChart.setStyle("-fx-background-color: lightgray");
    rangeChart.setTitle("Range Chart");
    rangeChart.setStyle("-fx-background-color: lightgray");

    XYChart.Series<Number, Number> rangeData = new XYChart.Series<>();
    rangeData.setName("range data");//red line
    rangeData.getData().add(new XYChart.Data<>(0,10));

    XYChart.Series<Number, Number> rangeAverage = new XYChart.Series<>();
    rangeAverage.setName("range average");//orange line
    rangeAverage.getData().add(new XYChart.Data<>(5,11));
    rangeAverage.getData().add(new XYChart.Data<>(10,15));

    XYChart.Series<Number, Number> averageData = new XYChart.Series<>();
    averageData.setName("average data");//red line
    averageData.getData().add(new XYChart.Data<>(0,10));

    XYChart.Series<Number, Number> averageAverage = new XYChart.Series<>();
    averageAverage.setName("average");//orange line
    averageAverage.getData().add(new XYChart.Data<>(5,11));
    averageAverage.getData().add(new XYChart.Data<>(10,15));


    //Add both to the chart
    rangeChart.getData().add(rangeData);
    rangeChart.getData().add(rangeAverage);
    averageChart.getData().add(averageData);
    averageChart.getData().add(averageAverage);

    BorderPane root = new BorderPane();
    root.setRight(averageChart);
    root.setLeft(rangeChart);

    //Build scene and show
    Scene scene = new Scene(root, 1200, 800);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

 */
