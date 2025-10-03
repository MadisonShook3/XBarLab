import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage stage) {
    RSCharts ui = new RSCharts();
    stage.setTitle("R & S Charts (n=10) — CSV");
    stage.setScene(new Scene(ui.getRoot(), 1100, 600));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
