package ca.tech.sense.it.smart.indoor.parking.system.model;

import java.util.Date;

public class Booking {
    private String userName;
    private Date bookingTime;
    private String paymentMethod;
    private String status; // e.g., "active", "upcoming", "completed"

    public Booking(String userName, Date bookingTime, String paymentMethod, String status) {
        this.userName = userName;
        this.bookingTime = bookingTime;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    // Getters and setters
    public String getUserName() { return userName; }
    public Date getBookingTime() { return bookingTime; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }

    public void setUserName(String userName) { this.userName = userName; }
    public void setBookingTime(Date bookingTime) { this.bookingTime = bookingTime; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status) { this.status = status; }
}
