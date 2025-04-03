package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class LoginManager {
    private DatabaseConnection dbConnection = new DatabaseConnection();
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 500;
    private static final int PREF_WIDTH = 600;
    private static final int PREF_HEIGHT = 700;

    public void showLoginScreen(Stage stage) {
        // Main container
        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(40));
        loginLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("Movie Booking System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Form container - will center in the available space
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(400);
        
        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        
        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        
        // Buttons container
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(120);
        registerButton.setPrefHeight(40);
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        // Status label
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #d32f2f;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(400);

        // Build form
        formBox.getChildren().addAll(
            usernameField, 
            passwordField, 
            buttonBox,
            statusLabel
        );
        
        // Add all to main layout
        loginLayout.getChildren().addAll(titleLabel, formBox);

        // Handle Login Button Click
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter both username and password.");
                return;
            }

            try {
                User user = dbConnection.validateUser(username, password);
                if (user != null) {
                    user.showDashboard(stage);
                } else {
                    statusLabel.setText("Invalid username or password");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error connecting to database: " + ex.getMessage());
            }
        });

        // Handle Register Button Click
        registerButton.setOnAction(e -> registerUser(stage));

        Scene scene = new Scene(loginLayout, PREF_WIDTH, PREF_HEIGHT);
        
        // Center the form when window is maximized
        loginLayout.setAlignment(Pos.CENTER);
        
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Start maximized but not fullscreen
        stage.setMaximized(true);
        stage.show();
    }

    private void registerUser(Stage stage) {
        // Main container
        VBox registerLayout = new VBox(20);
        registerLayout.setAlignment(Pos.CENTER);
        registerLayout.setPadding(new Insets(40));
        registerLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Form container
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(400);
        
        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setPrefHeight(40);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        
        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.setPrefHeight(40);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        
        // Register button
        Button submitButton = new Button("Register");
        submitButton.setPrefWidth(120);
        submitButton.setPrefHeight(40);
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        // Back button
        Button backButton = new Button("Back to Login");
        backButton.setPrefWidth(120);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
        
        // Status label
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #d32f2f;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(400);

        // Build form
        formBox.getChildren().addAll(
            usernameField, 
            passwordField, 
            submitButton,
            backButton,
            statusLabel
        );
        
        // Add all to main layout
        registerLayout.getChildren().addAll(titleLabel, formBox);

        // Handle Register Submission
        submitButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter a username and password.");
                return;
            }

            try {
                boolean success = dbConnection.insertUser(username, password);
                if (success) {
                    statusLabel.setText("Registration successful! You can now log in.");
                    statusLabel.setStyle("-fx-text-fill: #388E3C;");
                } else {
                    statusLabel.setText("Registration failed. Username may already exist.");
                    statusLabel.setStyle("-fx-text-fill: #d32f2f;");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #d32f2f;");
            }
        });

        // Handle Back Button
        backButton.setOnAction(e -> showLoginScreen(stage));

        Scene registerScene = new Scene(registerLayout, PREF_WIDTH, PREF_HEIGHT);
        
        // Center the form when window is maximized
        registerLayout.setAlignment(Pos.CENTER);
        
        stage.setScene(registerScene);
        stage.setTitle("Register");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Keep window maximized when switching scenes
        stage.setMaximized(true);
        stage.show();
    }
}