/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

import java.util.Map;

public class ParkingLocation {
    private String id;
    private String ownerId;
    private String address;
    private double latitude;
    private double longitude;
    private String name;
    private String postalCode;
    private double price;
    private Map<String, ParkingSlot> slots;

    // Constructors
    public ParkingLocation() {
    }


    public ParkingLocation(String id, String ownerId, Map<String, ParkingSlot> slots, String postalCode, String name, double longitude,  double latitude, String address, double price) {
        this.id = id;
        this.slots = slots;
        this.postalCode = postalCode;
        this.name = name;
        this.longitude = longitude;
        this.address = address;
        this.latitude = latitude;
        this.price = price;
        this.ownerId = ownerId;
    }

    // Getters and setters

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public double getPrice() {
        return price; // Getter for price
    }

    public void setPrice(double price) {
        this.price = price; // Setter for price
    }

    public Map<String, ParkingSlot> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, ParkingSlot> slots) {
        this.slots = slots;
    }
}
