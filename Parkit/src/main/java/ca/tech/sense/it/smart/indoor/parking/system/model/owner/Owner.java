package ca.tech.sense.it.smart.indoor.parking.system.model.owner;

public class Owner {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profilePhotoUrl;

    // Default constructor required for calls to DataSnapshot.getValue(Owner.class)
    public Owner() {
    }

    // Parameterized constructor
    public Owner(String uid, String firstName, String lastName, String email, String phone, String profilePhotoUrl) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
}
