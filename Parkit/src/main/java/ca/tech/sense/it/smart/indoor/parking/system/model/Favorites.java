package ca.tech.sense.it.smart.indoor.parking.system.model;

public class Favorites {
    private String id;
    private String address;
    private String name;
    private String postalCode;

    public Favorites() {
    }

    public Favorites(String id, String address, String name, String postalCode) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.postalCode = postalCode;
    }

    // Getters and setters


    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}

