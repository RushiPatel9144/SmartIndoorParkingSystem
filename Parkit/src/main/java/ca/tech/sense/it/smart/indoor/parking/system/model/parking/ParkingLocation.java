package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

import java.util.Map;

public class ParkingLocation {
    private String id;
    private String address;
    private double latitude;
    private double longitude;
    private String name;
    private String postalCode;
    private Map<String, ParkingSlot> slots;

    // Constructors
    public ParkingLocation() {
    }

    public ParkingLocation(String id, Map<String, ParkingSlot> slots, String postalCode, String name, double longitude, String address, double latitude) {

        this.id = id;
        this.slots = slots;
        this.postalCode = postalCode;
        this.name = name;
        this.longitude = longitude;
        this.address = address;
        this.latitude = latitude;
    }

    // Getters and setters
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

    public Map<String, ParkingSlot> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, ParkingSlot> slots) {
        this.slots = slots;
    }
}
