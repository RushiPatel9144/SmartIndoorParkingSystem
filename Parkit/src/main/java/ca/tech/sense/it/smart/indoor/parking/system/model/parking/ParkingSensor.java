package ca.tech.sense.it.smart.indoor.parking.system.model.parking;

public class ParkingSensor {
    private int batteryLevel;
    private String lastUpdated;
    private String sensorId;
    private String type;

    // Constructors
    public ParkingSensor() {
    }

    public ParkingSensor(String sensorId, String lastUpdated, int batteryLevel , String type) {
        this.batteryLevel = batteryLevel;
        this.lastUpdated = lastUpdated;
        this.sensorId = sensorId;
        this.type = type;
    }

    // Getters and setters
    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
