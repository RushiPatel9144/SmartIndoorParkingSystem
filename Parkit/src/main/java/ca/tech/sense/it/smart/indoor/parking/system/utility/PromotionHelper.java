package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;

public class PromotionHelper {

    private static final String PROMOTIONS_NODE = "Promotions";
    private static final String TAG = "PromotionHelper";

    // Private constructor to prevent instantiation
    private PromotionHelper() {}

    public static void saveHardcodedPromotionsToFirebase() {
        Promotion newUser = createPromotion("New User", "Welcome to the app", 15);
        Promotion winterSale = createPromotion("Winter Sale", "Save money this winter", 20);

        savePromotionToFirebase(newUser);
        savePromotionToFirebase(winterSale);
    }

    private static Promotion createPromotion(String title, String description, int discount) {
        String promotionId = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE).push().getKey();
        return new Promotion(promotionId, title, description, discount);
    }

    private static void savePromotionToFirebase(Promotion promotion) {
        if (promotion == null || promotion.getId() == null) {
            Log.e(TAG, "Promotion or Promotion ID is null, cannot save to Firebase");
            return;
        }

        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        promotionsRef.child(promotion.getId()).setValue(promotion)
                .addOnSuccessListener(aVoid -> Log.d(TAG, promotion.getTitle() + " promotion saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save " + promotion.getTitle() + " promotion", e));
    }
}