package ca.tech.sense.it.smart.indoor.parking.system.model.booking;

import androidx.annotation.NonNull;

public class Transaction {
    private String transactionId;
    private String parkingAddress;
    private double price;
    private String paymentTime;
    private boolean isRefunded; // Indicates if a refund has been issued

    // Constructor
    public Transaction(String transactionId, String parkingAddress, double price, String paymentTime, boolean isRefunded) {
        this.transactionId = transactionId;
        this.parkingAddress = parkingAddress;
        this.price = price;
        this.paymentTime = paymentTime;
        this.isRefunded = isRefunded;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getParkingAddress() {
        return parkingAddress;
    }

    public void setParkingAddress(String parkingAddress) {
        this.parkingAddress = parkingAddress;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public boolean isRefunded() {
        return isRefunded;
    }

    public void setRefunded(boolean refunded) {
        isRefunded = refunded;
    }

}
