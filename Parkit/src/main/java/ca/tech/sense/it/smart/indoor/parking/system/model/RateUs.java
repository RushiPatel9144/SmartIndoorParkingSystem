/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.model;

import java.util.List;

public class RateUs {
    private float rating;
    private String comment;
    private String deviceModel;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userType;
    private List<String> selectedOptions;

    public RateUs() {
    }



    public RateUs(float rating, String comment, String deviceModel, String userName, String userEmail, String userPhone, String userType, List<String> selectedOptions) {
        this.rating = rating;
        this.comment = comment;
        this.deviceModel = deviceModel;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userType = userType;
        this.selectedOptions = selectedOptions;
    }

    // Getters and setters
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public List<String> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(List<String> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
