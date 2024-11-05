package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public class BookingStatus {
    private String status; // "occupied" or "available"
    private String bookingDate; // e.g., "2024-11-05"

    public BookingStatus(){}

    public BookingStatus(String status, String bookingDate) {
        this.status = status;
        this.bookingDate = bookingDate;
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
}
