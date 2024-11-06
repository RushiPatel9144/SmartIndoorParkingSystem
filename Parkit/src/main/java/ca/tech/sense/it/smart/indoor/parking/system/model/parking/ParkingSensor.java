/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
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
