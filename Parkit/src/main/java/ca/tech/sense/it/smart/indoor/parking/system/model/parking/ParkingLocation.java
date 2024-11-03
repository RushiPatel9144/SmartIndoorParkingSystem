package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

import java.util.Map;

public class ParkingLocation {
    private String id;
    private String address;
    private double latitude;
    private double longitude;
    private String name;
    private Map<String, ParkingSlot> slots;

    // Constructors
    public ParkingLocation() {
    }

    public ParkingLocation(String id, String address, double latitude, double longitude, String name, Map<String, ParkingSlot> slots) {
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.slots = slots;
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

    public Map<String, ParkingSlot> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, ParkingSlot> slots) {
        this.slots = slots;
    }
}
