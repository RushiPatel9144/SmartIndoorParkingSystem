package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.parkingHistory;

public class ParkingHistoryModel {
    private String locationName;
    private String slotName;
    private String paymentAmount;
    private String usageTime;
    private String userProfilePicUrl;

    // Constructor
    public ParkingHistoryModel(String locationName, String slotName, String paymentAmount, String usageTime, String userProfilePicUrl) {
        this.locationName = locationName;
        this.slotName = slotName;
        this.paymentAmount = paymentAmount;
        this.usageTime = usageTime;
        this.userProfilePicUrl = userProfilePicUrl;
    }

    // Getters and Setters
    public String getLocationName() { return locationName; }
    public String getSlotName() { return slotName; }
    public String getPaymentAmount() { return paymentAmount; }
    public String getUsageTime() { return usageTime; }
    public String getUserProfilePicUrl() { return userProfilePicUrl; }
}
