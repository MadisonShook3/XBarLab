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
  private double[] fileArray;//holds raw values from the file
  private double[] cusumArray;//holds values after cusum

  public void start(Stage primaryStage) {
    //Set up axis for line chart
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Point");
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Values");
    //Set up the actual chart
    LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("CUSUM Chart");
    lineChart.setStyle("-fx-background-color: lightgray");

    XYChart.Series<Number, Number> CUSUM_Data = new XYChart.Series<>();
    CUSUM_Data.setName("CUSUM");//red line

    XYChart.Series<Number, Number> originalData = new XYChart.Series<>();
    originalData.setName("Data set");//orange line

    //Add both to the chart
    lineChart.getData().add(CUSUM_Data);
    lineChart.getData().add(originalData);

    //Used to create side panel
    VBox sidePane = new VBox(10);
    sidePane.setStyle("-fx-background-color: lightgray");

    //Make buttons/textFields on the side panel
    Label bootstrapLabel = new Label("Bootstraps: ");
    Label confidenceLabel = new Label("Confidence % (ex. 95): ");
    Label fileNameLabel = new Label("File name: ");
    TextField bootStrapText = new TextField();
    TextField confidenceText = new TextField();
    TextField textFile = new TextField();
    Button applyButton = new Button("apply");
    Button exitButton = new Button("exit");

    //Actions for the apply button
    applyButton.setOnAction(e -> {
      //Get user inputs
      int numBootstraps = Integer.parseInt(bootStrapText.getText());
      double confidenceLevel = Double.parseDouble(confidenceText.getText()) / 100.0;
      String fileName = textFile.getText();

      //clear previous data
      CUSUM_Data.getData().clear();
      originalData.getData().clear();

      try {
        this.fileArray = FileReader.extractData(fileName);

        //Make sure confidence level is in the correct range
        CusumMath cusumMath = null;
        if((confidenceLevel*100) >=0 && (confidenceLevel*100) <= 100) {
          cusumMath = new CusumMath(numBootstraps, confidenceLevel);
        }else{
          System.out.println("Invalid confidence level\nExiting...");
          System.exit(0);
        }

        this.cusumArray = cusumMath.cusum(fileArray);

        //Change point detection
        ArrayList<CusumMath.ChangePoint> changePoints = cusumMath.findChanges(fileArray);
        for (CusumMath.ChangePoint c : changePoints) {
          System.out.println("Change point at: " + c.index() +
                  " With Confidence: " + c.confidence());
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }

      //Plot cusum and original data values
      for (int i = 0; i < cusumArray.length; i++) {
        CUSUM_Data.getData().add(new XYChart.Data<>(i+1, cusumArray[i]));
      }
      for (int i = 0; i < fileArray.length; i++) {
        originalData.getData().add(new XYChart.Data<>(i+1, fileArray[i]));
      }
    });

    //Quit program
    exitButton.setOnAction(e -> {
      System.exit(0);
    });

    //Add buttons/textFields to the side panel
    sidePane.getChildren().addAll(bootstrapLabel, bootStrapText, confidenceLabel,confidenceText, fileNameLabel,textFile,applyButton,exitButton);

    //Set layout
    BorderPane root = new BorderPane();
    root.setCenter(lineChart);
    root.setLeft(sidePane);

    //Build scene and show
    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

 */
