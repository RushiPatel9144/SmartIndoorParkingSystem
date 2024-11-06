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

    // Empty constructor needed for Firebase
    public Promotion() {}

    public Promotion(String id, String title, String description,int discount) {
        this.id=id;
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


}
