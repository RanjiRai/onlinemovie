package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    public Admin(int id, String username, String password, String role) {
        super(id, username, password, role);
    }

    @Override
    public void showDashboard(Stage stage) {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setAlignment(Pos.TOP_CENTER);
        dashboard.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title section
        Label titleLabel = new Label("Admin Dashboard - Welcome, " + getUsername());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");

        // Button grid
        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(20);
        buttonGrid.setPadding(new Insets(20));

        Button addMovieButton = createStyledButton("Add New Movie", "#4CAF50");
        addMovieButton.setOnAction(e -> showAddMovieScreen(stage));
        addMovieButton.setPrefSize(200, 60);

        Button viewMoviesButton = createStyledButton("View All Movies", "#2196F3");
        viewMoviesButton.setOnAction(e -> showAllMoviesScreen(stage));
        viewMoviesButton.setPrefSize(200, 60);

        Button viewBookingsButton = createStyledButton("View All Bookings", "#FF9800");
        viewBookingsButton.setOnAction(e -> showAllBookingsScreen(stage));
        viewBookingsButton.setPrefSize(200, 60);

        Button logoutButton = createStyledButton("Logout", "#F44336");
        logoutButton.setOnAction(e -> new LoginManager().showLoginScreen(stage));
        logoutButton.setPrefSize(200, 60);

        buttonGrid.add(addMovieButton, 0, 0);
        buttonGrid.add(viewMoviesButton, 1, 0);
        buttonGrid.add(viewBookingsButton, 0, 1);
        buttonGrid.add(logoutButton, 1, 1);

        dashboard.getChildren().addAll(titleLabel, buttonGrid);
        
        Scene scene = new Scene(dashboard, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Force maximized window covering entire screen
        forceMaximize(stage);
    }

    private void forceMaximize(Stage stage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        
        stage.show();
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px;");
        return button;
    }

    private void showAddMovieScreen(Stage stage) {
        VBox addMovieLayout = new VBox(20);
        addMovieLayout.setPadding(new Insets(30));
        addMovieLayout.setAlignment(Pos.TOP_CENTER);
        addMovieLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("Add New Movie");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));

        TextField titleField = createFormField("Movie Title:");
        TextField showTimeField = createFormField("Show Time (YYYY-MM-DD HH:MM):");
        TextField seatsField = createFormField("Total Seats:");

        formGrid.add(new Label("Movie Title:"), 0, 0);
        formGrid.add(titleField, 1, 0);
        formGrid.add(new Label("Show Time:"), 0, 1);
        formGrid.add(showTimeField, 1, 1);
        formGrid.add(new Label("Total Seats:"), 0, 2);
        formGrid.add(seatsField, 1, 2);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px;");

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button submitButton = createStyledButton("Add Movie", "#4CAF50");
        Button backButton = createStyledButton("Back to Dashboard", "#607D8B");

        submitButton.setOnAction(event -> {
            try {
                String title = titleField.getText().trim();
                String showTime = showTimeField.getText().trim();
                String seatsText = seatsField.getText().trim();

                if (title.isEmpty() || showTime.isEmpty() || seatsText.isEmpty()) {
                    statusLabel.setText("Please fill all fields!");
                    return;
                }

                int totalSeats = Integer.parseInt(seatsText);
                MovieManager movieManager = new MovieManager();
                movieManager.addMovie(title, showTime, totalSeats);
                
                statusLabel.setText("Movie added successfully with " + totalSeats + " seats!");
                statusLabel.setStyle("-fx-text-fill: #388E3C;");
                titleField.clear();
                showTimeField.clear();
                seatsField.clear();
            } catch (NumberFormatException e) {
                statusLabel.setText("Please enter a valid number for seats!");
                statusLabel.setStyle("-fx-text-fill: #d32f2f;");
            } catch (SQLException ex) {
                statusLabel.setText("Error adding movie: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #d32f2f;");
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> showDashboard(stage));

        buttonBox.getChildren().addAll(submitButton, backButton);
        addMovieLayout.getChildren().addAll(titleLabel, formGrid, statusLabel, buttonBox);
        
        Scene addMovieScene = new Scene(addMovieLayout, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(addMovieScene);
        stage.setTitle("Add New Movie");
        forceMaximize(stage);
    }

    private TextField createFormField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(300);
        field.setPrefHeight(35);
        field.setStyle("-fx-font-size: 14px;");
        return field;
    }

    private void showAllMoviesScreen(Stage stage) {
        VBox moviesLayout = new VBox(20);
        moviesLayout.setPadding(new Insets(20));
        moviesLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("All Movies");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ListView<String> moviesList = new ListView<>();
        moviesList.setStyle("-fx-font-size: 14px; -fx-background-color: white;");
        moviesList.setPrefHeight(500);

        ScrollPane scrollPane = new ScrollPane(moviesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: white;");

        try {
            MovieManager movieManager = new MovieManager();
            ArrayList<Movie> movies = movieManager.getAllMovies();
            
            if (movies.isEmpty()) {
                moviesList.getItems().add("No movies available.");
            } else {
                for (Movie movie : movies) {
                    List<String> availableSeats = movieManager.getAvailableSeats(movie.getId());
                    String seatInfo = availableSeats.isEmpty() ? "No seats available" : 
                        "Available seats: " + String.join(", ", availableSeats.subList(0, Math.min(5, availableSeats.size()))) + 
                        (availableSeats.size() > 5 ? "..." : "");
                    
                    moviesList.getItems().add(
                        String.format("ID: %d | %s\nShow Time: %s\n%s/%d seats available\n%s\n",
                            movie.getId(),
                            movie.getTitle(),
                            movie.getShowTime(),
                            availableSeats.size(),
                            movie.getTotalSeats(),
                            seatInfo
                        )
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            moviesList.getItems().add("Error loading movies: " + ex.getMessage());
        }

        Button backButton = createStyledButton("Back to Dashboard", "#607D8B");
        backButton.setOnAction(e -> showDashboard(stage));

        moviesLayout.getChildren().addAll(titleLabel, scrollPane, backButton);
        
        Scene moviesScene = new Scene(moviesLayout, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(moviesScene);
        stage.setTitle("All Movies");
        forceMaximize(stage);
    }

    private void showAllBookingsScreen(Stage stage) {
        VBox bookingsLayout = new VBox(20);
        bookingsLayout.setPadding(new Insets(20));
        bookingsLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("All Bookings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ListView<String> bookingsList = new ListView<>();
        bookingsList.setStyle("-fx-font-size: 14px; -fx-background-color: white;");
        bookingsList.setPrefHeight(500);

        ScrollPane scrollPane = new ScrollPane(bookingsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: white;");

        try {
            BookingManager bookingManager = new BookingManager();
            ArrayList<Booking> allBookings = bookingManager.getAllBookings();
            
            if (allBookings.isEmpty()) {
                bookingsList.getItems().add("No bookings found.");
            } else {
                for (Booking booking : allBookings) {
                    bookingsList.getItems().add(
                        String.format("Booking ID: %d\nUser: %s\nMovie: %s (%s)\nSeats: %s\nBooked on: %s\n%s\n",
                            booking.getId(),
                            booking.getUser().getUsername(),
                            booking.getMovie().getTitle(),
                            booking.getMovie().getShowTime(),
                            String.join(", ", booking.getSeats()),
                            booking.getBookingTime(),
                            "--------------------------------------------------"
                        )
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            bookingsList.getItems().add("Error loading bookings: " + ex.getMessage());
        }

        Button backButton = createStyledButton("Back to Dashboard", "#607D8B");
        backButton.setOnAction(e -> showDashboard(stage));

        bookingsLayout.getChildren().addAll(titleLabel, scrollPane, backButton);
        
        Scene bookingsScene = new Scene(bookingsLayout, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(bookingsScene);
        stage.setTitle("All Bookings");
        forceMaximize(stage);
    }
}