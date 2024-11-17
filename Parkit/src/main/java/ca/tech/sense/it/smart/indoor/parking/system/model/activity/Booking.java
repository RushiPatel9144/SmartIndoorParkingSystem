/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.model.activity;

import java.io.Serializable;

public class Booking implements Serializable {
    private String id;
    private String title;
    private long startTime;
    private long endTime;
    private String location;
    private double price;
    private String slotNumber;
    private String passKey;
    private String locationId; // Add this field

    // Default constructor required for calls to DataSnapshot.getValue(Booking.class)
    public Booking() {
    }

    // Add a constructor that includes the locationId
    public Booking(String title, long startTime, long endTime, String location, double price, String slotNumber, String passKey, String locationId) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.price = price;
        this.slotNumber = slotNumber;
        this.passKey = passKey;
        this.locationId = locationId;
    }

    // Getters and setters for all fields, including locationId
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
}
