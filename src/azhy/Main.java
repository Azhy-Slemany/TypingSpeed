package azhy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Typing Speed is a desktop application that can
 * count your typing test speed, with some more functionalities.
 *
 * @author Azhy Slemany
 * @version 1.0
 * @since 2020
 */

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("layouts/main.fxml"));
        primaryStage.setTitle("Typing Speed");
        primaryStage.setScene(new Scene(root, 550, 450));
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setResizable(false);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
