package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/movie_db2";
    private static final String USER = "root";
    private static final String PASSWORD = "dfamily13245";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Insert new user
    public boolean insertUser(String username, String password) throws SQLException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'customer')";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        }
    }

    // Validate user credentials
    public User validateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    if ("admin".equals(role)) {
                        return new Admin(id, username, password, role);
                    } else {
                        return new Customer(id, username, password, role);
                    }
                }
            }
        }
        return null;
    }

    // Get all movies
    public List<Movie> getMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM movies";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Movie movie = new Movie(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("showtime"),
                    rs.getInt("total_seats")
                );
                
                // Initialize seats for the movie
                initializeMovieSeats(conn, movie);
                movies.add(movie);
            }
        }
        return movies;
    }

    private void initializeMovieSeats(Connection conn, Movie movie) throws SQLException {
        // Check if seats already exist
        String checkQuery = "SELECT COUNT(*) FROM movie_seats WHERE movie_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, movie.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // No seats exist, create them
                    String insertQuery = "INSERT INTO movie_seats (movie_id, seat_number, is_available) VALUES (?, ?, TRUE)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        for (String seat : movie.getAvailableSeats()) {
                            insertStmt.setInt(1, movie.getId());
                            insertStmt.setString(2, seat);
                            insertStmt.addBatch();
                        }
                        insertStmt.executeBatch();
                    }
                }
            }
        }
    }

    // Save a new booking
    public void saveBooking(Booking booking) throws SQLException {
        String query = "INSERT INTO bookings (user_id, movie_id, seats, booking_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, booking.getUser().getId());
            stmt.setInt(2, booking.getMovie().getId());
            stmt.setString(3, String.join(",", booking.getSeats()));
            stmt.setString(4, booking.getBookingTime());
            stmt.executeUpdate();
        }
    }

    // Get user's booking history
    public List<Booking> getUserBookings(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, m.title, m.showtime, m.total_seats FROM bookings b " +
                       "JOIN movies m ON b.movie_id = m.id " +
                       "WHERE b.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("title"),
                        rs.getString("showtime"),
                        rs.getInt("total_seats")
                    );
                    User user = new Customer(
                        userId, "", "", "customer"
                    );
                    bookings.add(new Booking(
                        rs.getInt("id"),
                        user,
                        movie,
                        List.of(rs.getString("seats").split(",")),
                        rs.getString("booking_time")
                    ));
                }
            }
        }
        return bookings;
    }

    // Cancel a booking
    public boolean cancelBooking(int bookingId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // First get the booking details to release seats
                Booking booking = null;
                try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT b.*, m.title, m.showtime, m.total_seats FROM bookings b " +
                    "JOIN movies m ON b.movie_id = m.id WHERE b.id = ?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Movie movie = new Movie(
                                rs.getInt("movie_id"),
                                rs.getString("title"),
                                rs.getString("showtime"),
                                rs.getInt("total_seats")
                            );
                            User user = new Customer(
                                rs.getInt("user_id"), "", "", "customer"
                            );
                            booking = new Booking(
                                bookingId,
                                user,
                                movie,
                                List.of(rs.getString("seats").split(",")),
                                rs.getString("booking_time")
                            );
                        }
                    }
                }

                if (booking == null) {
                    return false;
                }

                // Release seats
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE movie_seats SET is_available = TRUE WHERE movie_id = ? AND seat_number = ?")) {
                    for (String seat : booking.getSeats()) {
                        ps.setInt(1, booking.getMovie().getId());
                        ps.setString(2, seat);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Delete booking
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM bookings WHERE id = ?")) {
                    ps.setInt(1, bookingId);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Check seat availability
    public boolean isSeatAvailable(int movieId, String seatNumber) throws SQLException {
        String query = "SELECT is_available FROM movie_seats WHERE movie_id = ? AND seat_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            stmt.setString(2, seatNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean("is_available");
            }
        }
    }
}