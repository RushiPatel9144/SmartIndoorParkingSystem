package ca.tech.sense.it.smart.indoor.parking.system.model;

public class Promotion {
    private String description;
    private int discount;
    private String title;

    // Empty constructor needed for Firebase
    public Promotion() {}

    public Promotion(String description, int discount, String title) {
        this.description = description;
        this.discount = discount;
        this.title = title;
    }

    // Getters
    public String getDescription() {
        return description;
    }

    public int getDiscount() {
        return discount;
    }

    public String getTitle() {
        return title;
    }
}
