package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

import java.util.HashMap;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSensor;

public class ParkingSlot {
    private String id;
    private ParkingSensor sensor;
    private Map<String, BookingStatus> hourlyStatus; // Key: "YYYY-MM-DD HH"

    // Constructors
    public ParkingSlot() {
        this.hourlyStatus = new HashMap<>();
    }

    public ParkingSlot(String id, ParkingSensor sensor) {
        this.id = id;
        this.sensor = sensor;
        this.hourlyStatus = new HashMap<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ParkingSensor getSensor() {
        return sensor;
    }

    public void setSensor(ParkingSensor sensor) {
        this.sensor = sensor;
    }

    public Map<String, BookingStatus> getHourlyStatus() {
        return hourlyStatus;
    }

    public void setHourlyStatus(Map<String, BookingStatus> hourlyStatus) {
        this.hourlyStatus = hourlyStatus;
    }

    // Method to set status for a specific date and hour
    public void setStatusForHour(String date, int hour, String status) {
        String key = String.format("%s %02d", date, hour); // Format: "YYYY-MM-DD HH"
        hourlyStatus.put(key, new BookingStatus(status, date));
    }

    // Method to get status for a specific date and hour
    public BookingStatus getStatusForHour(String date, int hour) {
        String key = String.format("%s %02d", date, hour);
        return hourlyStatus.get(key);
    }
}
