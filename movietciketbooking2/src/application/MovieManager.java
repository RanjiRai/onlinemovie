package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieManager {
    public ArrayList<Movie> getAllMovies() throws SQLException {
        ArrayList<Movie> movies = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM movies")) {
            
            while (rs.next()) {
                Movie movie = new Movie(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("showtime"),
                    rs.getInt("total_seats")
                );
                
                // Initialize seats for the movie
                initializeSeatsForMovie(conn, movie);
                movies.add(movie);
            }
        }
        return movies;
    }

    public void addMovie(String title, String showTime, int totalSeats) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // Insert movie
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO movies (title, showtime, total_seats) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                    
                    ps.setString(1, title);
                    ps.setString(2, showTime);
                    ps.setInt(3, totalSeats);
                    ps.executeUpdate();
                    
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int movieId = rs.getInt(1);
                            Movie movie = new Movie(movieId, title, showTime, totalSeats);
                            initializeSeatsForMovie(conn, movie);
                        }
                    }
                }
                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            }
        }
    }

    public void updateMovie(Movie movie) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE movies SET title=?, showtime=?, total_seats=? WHERE id=?")) {
            
            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getShowTime());
            ps.setInt(3, movie.getTotalSeats());
            ps.setInt(4, movie.getId());
            ps.executeUpdate();
        }
    }

    public void deleteMovie(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // First delete all seats for the movie
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM movie_seats WHERE movie_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // Then delete the movie
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM movies WHERE id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            }
        }
    }

    private void initializeSeatsForMovie(Connection conn, Movie movie) throws SQLException {
        // Check if seats already exist
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT COUNT(*) FROM movie_seats WHERE movie_id=?")) {
            ps.setInt(1, movie.getId());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // No seats exist, create them
                    try (PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO movie_seats (movie_id, seat_number, is_available) VALUES (?, ?, TRUE)")) {
                        
                        int rows = (int) Math.ceil(movie.getTotalSeats() / 10.0);
                        for (int i = 1; i <= rows; i++) {
                            char rowLetter = (char) ('A' + i - 1);
                            for (int j = 1; j <= 10; j++) {
                                if (((i-1)*10 + j) <= movie.getTotalSeats()) {
                                    String seatNumber = rowLetter + String.valueOf(j);
                                    insertPs.setInt(1, movie.getId());
                                    insertPs.setString(2, seatNumber);
                                    insertPs.addBatch();
                                }
                            }
                        }
                        insertPs.executeBatch();
                    }
                }
            }
        }
    }

    public List<String> getAvailableSeats(int movieId) throws SQLException {
        List<String> availableSeats = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT seat_number FROM movie_seats WHERE movie_id=? AND is_available=TRUE ORDER BY seat_number")) {
            
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availableSeats.add(rs.getString("seat_number"));
                }
            }
        }
        return availableSeats;
    }
}