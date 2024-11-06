/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.utility;

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
