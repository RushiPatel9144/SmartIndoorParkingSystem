package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    private static BookingManager instance;
    private List<Booking> bookings; // Assuming Booking is a class representing a booking

    private BookingManager() {
        bookings = new ArrayList<>(); // Initialize your bookings list
    }

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void saveBooking(String name, String timing, Date date) {
        // Implementation to save booking details
    }

    // Additional methods to manage bookings (e.g., getActiveBookings, getUpcomingBookings, etc.)
}
