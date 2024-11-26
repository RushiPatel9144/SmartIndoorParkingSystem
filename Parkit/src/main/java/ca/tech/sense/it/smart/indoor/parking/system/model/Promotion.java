/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.model;

public class Promotion {
    private String id;
    private String title;
    private String description;
    private int discount;
    private String promoCode = ""; // Default value
    private boolean used = false;  // Default value

    // Empty constructor needed for Firebase
    public Promotion() {}

    public Promotion(String id, String title, String description, int discount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.discount = discount;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDiscount() {
        return discount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public boolean isUsed() {
        return used;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDiscount(int discount) {
        if (discount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative.");
        }
        this.discount = discount;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", discount=" + discount +
                ", promoCode='" + promoCode + '\'' +
                ", used=" + used +
                '}';
    }
}
