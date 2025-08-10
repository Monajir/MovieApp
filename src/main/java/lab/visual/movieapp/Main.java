package lab.visual.movieapp;

import javafx.application.Platform;
import lab.visual.movieapp.service.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1400, 900);

        // Apply initial theme
        ThemeManager.getInstance().setScene(scene);
        ThemeManager.getInstance().applyTheme();

        primaryStage.setTitle("MovFlix - Modern Movie Browser");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();

        // Handle window close event to properly shut down the application
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    @Override
    public void stop() throws Exception {
        // Clean up resources when application is closing
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}