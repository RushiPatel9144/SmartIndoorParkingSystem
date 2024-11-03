package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

public class ParkingSlot {
    private String id;
    private ParkingSensor sensor;
    private String status;

    // Constructors
    public ParkingSlot() {
    }

    public ParkingSlot(String id, ParkingSensor sensor, String status) {
        this.id = id;
        this.sensor = sensor;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
