package ca.tech.sense.it.smart.indoor.parking.system.model;

import java.util.Date;

public class BookingDetails {
    private String userId;
    private String userName;
    private String selectedTimeSlot;
    private String address;
    private String postalCode;
    private Date bookingDate;

    public BookingDetails(String userId, String userName, String selectedTimeSlot, String address, String postalCode, Date bookingDate) {
        this.userId = userId;
        this.userName = userName;
        this.selectedTimeSlot = selectedTimeSlot;
        this.address = address;
        this.postalCode = postalCode;
        this.bookingDate = bookingDate;
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSelectedTimeSlot() {
        return selectedTimeSlot;
    }

    public void setSelectedTimeSlot(String selectedTimeSlot) {
        this.selectedTimeSlot = selectedTimeSlot;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }
}
