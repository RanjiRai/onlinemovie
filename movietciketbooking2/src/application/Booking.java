package application;

import java.util.List;

public class Booking {
    private int id;
    private User user;
    private Movie movie;
    private List<String> seats;
    private String bookingTime;

    public Booking(int id, User user, Movie movie, List<String> seats, String bookingTime) {
        this.id = id;
        this.user = user;
        this.movie = movie;
        this.seats = seats;
        this.bookingTime = bookingTime;
    }

    // Getters
    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Movie getMovie() {
        return movie;
    }

    public List<String> getSeats() {
        return seats;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    @Override
    public String toString() {
        return "Booking #" + id + " - " + user.getUsername() + " - " + 
               movie.getTitle() + " (" + bookingTime + ") - Seats: " + 
               String.join(", ", seats);
    }
}