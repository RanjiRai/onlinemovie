package application;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    public Customer(int id, String username, String password, String role) {
        super(id, username, password, role);
    }

    @Override
    public void showDashboard(Stage stage) {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(30));
        dashboard.setAlignment(Pos.TOP_CENTER);
        dashboard.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Welcome, " + getUsername());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Button grid
        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(20);
        buttonGrid.setPadding(new Insets(20));

        Button viewMoviesButton = createStyledButton("View Movies", "#2196F3");
        viewMoviesButton.setOnAction(e -> showMovies(stage));
        viewMoviesButton.setPrefSize(200, 60);

        Button bookTicketButton = createStyledButton("Book Tickets", "#4CAF50");
        bookTicketButton.setOnAction(e -> bookTicket(stage));
        bookTicketButton.setPrefSize(200, 60);

        Button viewHistoryButton = createStyledButton("My Bookings", "#FF9800");
        viewHistoryButton.setOnAction(e -> viewBookingHistory(stage));
        viewHistoryButton.setPrefSize(200, 60);

        Button logoutButton = createStyledButton("Logout", "#F44336");
        logoutButton.setOnAction(e -> new LoginManager().showLoginScreen(stage));
        logoutButton.setPrefSize(200, 60);

        buttonGrid.add(viewMoviesButton, 0, 0);
        buttonGrid.add(bookTicketButton, 1, 0);
        buttonGrid.add(viewHistoryButton, 0, 1);
        buttonGrid.add(logoutButton, 1, 1);

        dashboard.getChildren().addAll(titleLabel, buttonGrid);
        
        Scene scene = new Scene(dashboard, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Customer Dashboard");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        forceMaximize(stage);  // Force maximize the window
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

    private void showMovies(Stage stage) {
        VBox moviesLayout = new VBox(20);
        moviesLayout.setPadding(new Insets(20));
        moviesLayout.setAlignment(Pos.TOP_CENTER);
        moviesLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("Available Movies");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ListView<VBox> movieList = new ListView<>();
        movieList.setStyle("-fx-font-size: 14px; -fx-background-color: white;");
        movieList.setPrefHeight(400);

        try {
            MovieManager movieManager = new MovieManager();
            List<Movie> movies = movieManager.getAllMovies();
            
            for (Movie movie : movies) {
                VBox movieEntry = new VBox(10);
                movieEntry.setPadding(new Insets(15));
                movieEntry.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: white;");

                Label title = new Label(movie.getTitle());
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                Label showTime = new Label("Show Time: " + movie.getShowTime());
                showTime.setStyle("-fx-font-size: 14px;");
                
                List<String> availableSeats = movieManager.getAvailableSeats(movie.getId());
                int availableCount = availableSeats.size();
                String seatsPreview = availableSeats.isEmpty() ? "No seats available" : 
                    String.join(", ", availableSeats.subList(0, Math.min(5, availableSeats.size()))) + 
                    (availableSeats.size() > 5 ? "..." : "");
                
                Label seats = new Label(
                    String.format("Available: %d/%d seats (%s)", 
                        availableCount, 
                        movie.getTotalSeats(), 
                        seatsPreview)
                );
                seats.setStyle("-fx-font-size: 14px; -fx-text-fill: " + 
                    (availableCount > 0 ? "#388E3C" : "#D32F2F") + ";");

                movieEntry.getChildren().addAll(title, showTime, seats);
                movieEntry.setUserData(movie);
                movieList.getItems().add(movieEntry);
            }
        } catch (SQLException ex) {
            showErrorAlert("Error", "Failed to load movies: " + ex.getMessage());
        }

        Button backButton = createStyledButton("Back to Dashboard", "#607D8B");
        backButton.setOnAction(e -> showDashboard(stage));

        moviesLayout.getChildren().addAll(titleLabel, movieList, backButton);
        
        Scene moviesScene = new Scene(moviesLayout, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(moviesScene);
        stage.setTitle("Available Movies");
        forceMaximize(stage);  // Force maximize the window
    }

    private void bookTicket(Stage stage) {
        VBox bookingLayout = new VBox(20);
        bookingLayout.setPadding(new Insets(20));
        bookingLayout.setAlignment(Pos.TOP_CENTER);
        bookingLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("Book Tickets");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        try {
            MovieManager movieManager = new MovieManager();
            List<Movie> movies = movieManager.getAllMovies();

            // Movie selection
            HBox movieBox = new HBox(10);
            movieBox.setAlignment(Pos.CENTER_LEFT);
            Label movieLabel = new Label("Select Movie:");
            movieLabel.setStyle("-fx-font-size: 16px;");
            
            ComboBox<Movie> movieCombo = new ComboBox<>();
            movieCombo.getItems().addAll(movies);
            movieCombo.setCellFactory(param -> new ListCell<Movie>() {
                @Override
                protected void updateItem(Movie movie, boolean empty) {
                    super.updateItem(movie, empty);
                    setText(empty || movie == null ? null : movie.getTitle() + " (" + movie.getShowTime() + ")");
                }
            });
            movieCombo.setButtonCell(new ListCell<Movie>() {
                @Override
                protected void updateItem(Movie movie, boolean empty) {
                    super.updateItem(movie, empty);
                    setText(empty || movie == null ? "Select a movie" : movie.getTitle() + " (" + movie.getShowTime() + ")");
                }
            });
            movieCombo.setPrefWidth(400);
            movieBox.getChildren().addAll(movieLabel, movieCombo);

            // Seat selection
            Label seatLabel = new Label("Select Seats:");
            seatLabel.setStyle("-fx-font-size: 16px;");
            seatLabel.setVisible(false);
            
            GridPane seatGrid = new GridPane();
            seatGrid.setHgap(5);
            seatGrid.setVgap(5);
            seatGrid.setPadding(new Insets(10));
            seatGrid.setStyle("-fx-background-color: white; -fx-padding: 10;");
            
            Label selectedSeatsLabel = new Label("Selected seats: None");
            selectedSeatsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            List<String> selectedSeats = new ArrayList<>();

            // Update seat grid when movie is selected
            movieCombo.valueProperty().addListener((obs, oldVal, selectedMovie) -> {
                seatGrid.getChildren().clear();
                selectedSeats.clear();
                selectedSeatsLabel.setText("Selected seats: None");
                
                if (selectedMovie != null) {
                    try {
                        List<String> availableSeats = movieManager.getAvailableSeats(selectedMovie.getId());
                        seatLabel.setVisible(true);

                        // Create seat buttons
                        int totalSeats = selectedMovie.getTotalSeats();
                        int rows = (int) Math.ceil(totalSeats / 10.0);
                        char rowLetter = 'A';
                        
                        for (int row = 0; row < rows; row++) {
                            Label rowLabel = new Label(rowLetter + ":");
                            rowLabel.setStyle("-fx-font-weight: bold;");
                            seatGrid.add(rowLabel, 0, row);
                            
                            for (int col = 1; col <= 10; col++) {
                                int seatNumber = (row * 10) + col;
                                if (seatNumber > totalSeats) break;
                                
                                String seatId = rowLetter + String.valueOf(col);
                                ToggleButton seatButton = new ToggleButton(String.valueOf(col));
                                seatButton.setUserData(seatId);
                                seatButton.setPrefSize(35, 35);
                                
                                if (availableSeats.contains(seatId)) {
                                    seatButton.setStyle("-fx-base: #4CAF50;"); // Green for available
                                    
                                    seatButton.selectedProperty().addListener((obsBtn, wasSelected, isSelected) -> {
                                        if (isSelected) {
                                            selectedSeats.add(seatId);
                                        } else {
                                            selectedSeats.remove(seatId);
                                        }
                                        selectedSeatsLabel.setText("Selected seats: " + 
                                            (selectedSeats.isEmpty() ? "None" : String.join(", ", selectedSeats)));
                                    });
                                } else {
                                    seatButton.setStyle("-fx-base: #F44336;"); // Red for taken
                                    seatButton.setDisable(true);
                                }
                                
                                seatGrid.add(seatButton, col, row);
                            }
                            rowLetter++;
                        }
                    } catch (SQLException ex) {
                        showErrorAlert("Error", "Could not load seat availability");
                    }
                } else {
                    seatLabel.setVisible(false);
                }
            });

            // Confirm booking button
            Button confirmButton = createStyledButton("Confirm Booking", "#4CAF50");
            confirmButton.setDisable(true);
            
            selectedSeatsLabel.textProperty().addListener((obs, oldText, newText) -> {
                confirmButton.setDisable(newText.equals("Selected seats: None"));
            });

            confirmButton.setOnAction(event -> {
                Movie selectedMovie = movieCombo.getValue();
                if (selectedMovie == null || selectedSeats.isEmpty()) {
                    showErrorAlert("Error", "Please select a movie and at least one seat");
                    return;
                }

                try {
                    BookingManager bookingManager = new BookingManager();
                    bookingManager.createBooking(this, selectedMovie, selectedSeats);
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Booking Confirmed");
                    successAlert.setContentText("Successfully booked seats: " + 
                        String.join(", ", selectedSeats) + " for " + selectedMovie.getTitle());
                    successAlert.showAndWait();
                    
                    // Reset form
                    movieCombo.setValue(null);
                    seatGrid.getChildren().clear();
                    selectedSeats.clear();
                    selectedSeatsLabel.setText("Selected seats: None");
                } catch (SQLException ex) {
                    showErrorAlert("Booking Error", ex.getMessage());
                }
            });

            Button backButton = createStyledButton("Back to Dashboard", "#607D8B");
            backButton.setOnAction(e -> showDashboard(stage));

            bookingLayout.getChildren().addAll(
                titleLabel,
                movieBox,
                seatLabel,
                new ScrollPane(seatGrid),
                selectedSeatsLabel,
                confirmButton,
                backButton
            );
        } catch (SQLException ex) {
            showErrorAlert("Error", "Failed to load movies: " + ex.getMessage());
        }
        
        Scene bookingScene = new Scene(bookingLayout, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(bookingScene);
        stage.setTitle("Book Tickets");
        forceMaximize(stage);  // Force maximize the window
    }

    private void viewBookingHistory(Stage stage) {
        VBox historyLayout = new VBox(20);
        historyLayout.setPadding(new Insets(20));
        historyLayout.setAlignment(Pos.TOP_CENTER);
        historyLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("My Bookings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        try {
            BookingManager bookingManager = new BookingManager();
            ListView<String> historyList = new ListView<>();
            historyList.setStyle("-fx-font-size: 14px; -fx-background-color: white;");
            historyList.setPrefHeight(400);
            
            List<Booking> bookings = bookingManager.getUserHistory(this);
            
            if (bookings.isEmpty()) {
                historyList.getItems().add("No bookings found");
            } else {
                for (Booking booking : bookings) {
                    historyList.getItems().add(
                        String.format("Movie: %s\nShowtime: %s\nSeats: %s\nBooked on: %s\n",
                            booking.getMovie().getTitle(),
                            booking.getMovie().getShowTime(),
                            String.join(", ", booking.getSeats()),
                            booking.getBookingTime()
                        )
                    );
                }
            }

            Button cancelButton = createStyledButton("Cancel Booking", "#F44336");
            Label statusLabel = new Label();
            statusLabel.setStyle("-fx-text-fill: #D32F2F;");

            cancelButton.setOnAction(e -> {
                int selectedIndex = historyList.getSelectionModel().getSelectedIndex();
                if (selectedIndex < 0 || bookings.isEmpty()) {
                    statusLabel.setText("Please select a booking to cancel.");
                    return;
                }
                
                try {
                    Booking selected = bookings.get(selectedIndex);
                    bookingManager.cancelBooking(selected);
                    historyList.getItems().remove(selectedIndex);
                    bookings.remove(selectedIndex);
                    statusLabel.setStyle("-fx-text-fill: #388E3C;");
                    statusLabel.setText("Booking canceled successfully!");
                } catch (SQLException ex) {
                    statusLabel.setText("Error canceling booking: " + ex.getMessage());
                }
            });

            Button backButton = createStyledButton("Back to Dashboard", "#607D8B");
            backButton.setOnAction(e -> showDashboard(stage));

            historyLayout.getChildren().addAll(
                titleLabel,
                historyList,
                cancelButton,
                statusLabel,
                backButton
            );
        } catch (SQLException ex) {
            showErrorAlert("Error", "Failed to load booking history: " + ex.getMessage());
        }
        
        Scene historyScene = new Scene(historyLayout, MIN_WIDTH, MIN_HEIGHT);
        stage.setScene(historyScene);
        stage.setTitle("Booking History");
        forceMaximize(stage);  // Force maximize the window
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}