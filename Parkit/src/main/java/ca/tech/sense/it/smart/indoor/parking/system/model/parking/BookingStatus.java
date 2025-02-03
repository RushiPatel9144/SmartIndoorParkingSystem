package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

public class BookingStatus {
    private String status; // "occupied" or "available"
    private String bookingDate; // e.g., "2024-11-05"
    private boolean carParked; // New field

    public BookingStatus() {
        this.carParked = false; // Default value
    }

    public BookingStatus(String status, String bookingDate, boolean carParked) {
        this.status = status;
        this.bookingDate = bookingDate;
        this.carParked = carParked;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public boolean isCarParked() {
        return carParked;
    }

    public void setCarParked(boolean carParked) {
        this.carParked = carParked;
    }
}
