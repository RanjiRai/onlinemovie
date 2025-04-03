package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    
    // Create a new booking with seats
    public void createBooking(User user, Movie movie, List<String> seatNumbers) throws SQLException {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new SQLException("No seats selected for booking");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // 1. Create the booking record
                int bookingId;
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bookings (user_id, movie_id, booking_time) VALUES (?, ?, CURRENT_TIMESTAMP)", 
                    Statement.RETURN_GENERATED_KEYS)) {
                    
                    ps.setInt(1, user.getId());
                    ps.setInt(2, movie.getId());
                    ps.executeUpdate();
                    
                    // Get the auto-generated booking ID
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            bookingId = rs.getInt(1);
                        } else {
                            throw new SQLException("Creating booking failed, no ID obtained.");
                        }
                    }
                }
                
                // 2. Reserve the seats
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO booking_seats (booking_id, seat_id) " +
                    "SELECT ?, id FROM movie_seats " +
                    "WHERE movie_id = ? AND seat_number = ? AND is_available = TRUE")) {
                    
                    for (String seatNumber : seatNumbers) {
                        ps.setInt(1, bookingId);
                        ps.setInt(2, movie.getId());
                        ps.setString(3, seatNumber);
                        ps.addBatch();
                    }
                    int[] insertResults = ps.executeBatch();
                    for (int result : insertResults) {
                        if (result != 1) {
                            throw new SQLException("Failed to reserve one or more seats");
                        }
                    }
                }
                
                // 3. Mark seats as unavailable
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE movie_seats SET is_available = FALSE " +
                    "WHERE movie_id = ? AND seat_number = ?")) {
                    
                    for (String seatNumber : seatNumbers) {
                        ps.setInt(1, movie.getId());
                        ps.setString(2, seatNumber);
                        ps.addBatch();
                    }
                    int[] updateResults = ps.executeBatch();
                    for (int result : updateResults) {
                        if (result != 1) {
                            throw new SQLException("Failed to update seat availability");
                        }
                    }
                }
                
                conn.commit(); // Commit the transaction
                
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw new SQLException("Booking failed: " + e.getMessage(), e);
            }
        }
    }
    
    // Get booking history for a specific user
    public ArrayList<Booking> getUserHistory(User user) throws SQLException {
        ArrayList<Booking> bookings = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT b.id, b.movie_id, b.booking_time, " +
                 "m.title, m.showtime " +
                 "FROM bookings b " +
                 "JOIN movies m ON b.movie_id = m.id " +
                 "WHERE b.user_id = ?")) {
            
            ps.setInt(1, user.getId());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("title"),
                        rs.getString("showtime"),
                        0 // Total seats not needed here
                    );
                    
                    // Get seats for this booking
                    List<String> seats = getSeatsForBooking(conn, rs.getInt("id"));
                    
                    bookings.add(new Booking(
                        rs.getInt("id"),
                        user,
                        movie,
                        seats,
                        rs.getString("booking_time")
                    ));
                }
            }
        }
        return bookings;
    }
    
    // Get all bookings with seat information (for admin view)
    public ArrayList<Booking> getAllBookings() throws SQLException {
        ArrayList<Booking> bookings = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT b.id, b.user_id, b.movie_id, b.booking_time, " +
                 "u.username, m.title, m.showtime " +
                 "FROM bookings b " +
                 "JOIN users u ON b.user_id = u.id " +
                 "JOIN movies m ON b.movie_id = m.id")) {
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new Customer(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        "", 
                        "customer"
                    );
                    
                    Movie movie = new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("title"),
                        rs.getString("showtime"),
                        0 // Total seats not needed here
                    );
                    
                    // Get seats for this booking
                    List<String> seats = getSeatsForBooking(conn, rs.getInt("id"));
                    
                    bookings.add(new Booking(
                        rs.getInt("id"),
                        user,
                        movie,
                        seats,
                        rs.getString("booking_time")
                    ));
                }
            }
        }
        return bookings;
    }
    
    // Helper method to get seats for a booking
    private List<String> getSeatsForBooking(Connection conn, int bookingId) throws SQLException {
        List<String> seats = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(
             "SELECT ms.seat_number " +
             "FROM booking_seats bs " +
             "JOIN movie_seats ms ON bs.seat_id = ms.id " +
             "WHERE bs.booking_id = ?")) {
            
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(rs.getString("seat_number"));
                }
            }
        }
        return seats;
    }
    
    // Cancel a booking and release seats
    public void cancelBooking(Booking booking) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // 1. Release the seats
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE movie_seats ms " +
                    "JOIN booking_seats bs ON ms.id = bs.seat_id " +
                    "SET ms.is_available = TRUE " +
                    "WHERE bs.booking_id = ?")) {
                    
                    ps.setInt(1, booking.getId());
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("No seats found for booking");
                    }
                }
                
                // 2. Remove booking-seat associations
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM booking_seats WHERE booking_id = ?")) {
                    
                    ps.setInt(1, booking.getId());
                    ps.executeUpdate();
                }
                
                // 3. Delete the booking
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM bookings WHERE id = ?")) {
                    
                    ps.setInt(1, booking.getId());
                    int affectedRows = ps.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("No booking found with ID: " + booking.getId());
                    }
                }
                
                conn.commit(); // Commit the transaction
                
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw new SQLException("Failed to cancel booking: " + e.getMessage(), e);
            }
        }
    }
}