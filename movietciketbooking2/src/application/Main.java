package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        LoginManager loginManager = new LoginManager();
        loginManager.showLoginScreen(stage);
        stage.setTitle("Movie Booking System");
        stage.show();
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}