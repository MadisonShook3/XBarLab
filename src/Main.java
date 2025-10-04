import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage stage) {

    //Uncomment Step 1 if you want to run that chart, and uncomment line 14 if you want to run step 2 chart
    //STEP 1 (X̄ & R Charts)
    RSCharts ui = new RSCharts();

    //STEP 2 (X̄ & S Charts)
    //XSCharts ui = new XSCharts();

    stage.setTitle("Control Charts (n=10) — CSV");
    stage.setScene(new Scene(ui.getRoot(), 1120, 620));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
