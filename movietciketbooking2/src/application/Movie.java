package application;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    private int id;
    private String title;
    private String showTime;
    private int totalSeats;
    private List<String> availableSeats;

    public Movie(int id, String title, String showTime, int totalSeats) {
        this.id = id;
        this.title = title;
        this.showTime = showTime;
        this.totalSeats = totalSeats;
        this.availableSeats = new ArrayList<>();
        initializeSeats();
    }

    private void initializeSeats() {
        // Initialize seats (e.g., A1, A2, ..., B1, B2, etc.)
        int rows = (int) Math.ceil(totalSeats / 10.0);
        for (int i = 0; i < rows; i++) {
            char row = (char) ('A' + i);
            for (int j = 1; j <= 10; j++) {
                if (availableSeats.size() < totalSeats) {
                    availableSeats.add(row + String.valueOf(j));
                }
            }
        }
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShowTime() {
        return showTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public List<String> getAvailableSeats() {
        return new ArrayList<>(availableSeats);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public boolean isSeatAvailable(String seatNumber) {
        return availableSeats.contains(seatNumber);
    }

    public void bookSeat(String seatNumber) {
        availableSeats.remove(seatNumber);
    }

    public void releaseSeat(String seatNumber) {
        if (!availableSeats.contains(seatNumber)) {
            availableSeats.add(seatNumber);
        }
    }

    @Override
    public String toString() {
        return title + " - " + showTime + " (" + availableSeats.size() + "/" + totalSeats + " seats available)";
    }
}