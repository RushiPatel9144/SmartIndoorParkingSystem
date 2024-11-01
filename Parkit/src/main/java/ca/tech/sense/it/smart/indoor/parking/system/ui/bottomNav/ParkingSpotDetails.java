package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

public class ParkingSpotDetails {
    private String address;
    private String postcode;
    private int imageResId;

    public ParkingSpotDetails(String address, String postcode, int imageResId) {
        this.address = address;
        this.postcode = postcode;
        this.imageResId = imageResId;
    }

    public String getAddress() {
        return address;
    }

    public String getPostcode() {
        return postcode;
    }

    public int getImageResId() {
        return imageResId;
    }
}

