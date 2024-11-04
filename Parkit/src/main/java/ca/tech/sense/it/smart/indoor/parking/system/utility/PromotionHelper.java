package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;

public class PromotionHelper {

    private static final String PROMOTIONS_NODE = "Promotions";
    private static final String PROMOTIONS_FLAG_NODE = "promotions_added";
    private static final String TAG = "PromotionHelper";

    private PromotionHelper() {}

    public static void saveHardcodedPromotionsToFirebase() {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        DatabaseReference promotionsFlagRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_FLAG_NODE);

        // Check if promotions have already been added
        promotionsFlagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class)) {
                    Log.d(TAG, "Promotions have already been added to Firebase. Skipping...");
                } else {
                    // Promotions have not been added yet. Add hardcoded promotions.
                    addHardcodedPromotions(promotionsRef);

                    // Set the flag to true to prevent future additions
                    promotionsFlagRef.setValue(true)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Promotions flag set to true"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to set promotions flag", e));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to check promotions flag: ", databaseError.toException());
            }
        });
    }

    private static void addHardcodedPromotions(DatabaseReference promotionsRef) {
        // Promotion 1
        Promotion winterSale = new Promotion(promotionsRef.push().getKey(), "Winter Sale", "Save money this winter", 20);
        savePromotionToFirebase(promotionsRef, winterSale);

        // Promotion 2
        Promotion newUserPromo = new Promotion(promotionsRef.push().getKey(), "New User", "Welcome to the app", 15);
        savePromotionToFirebase(promotionsRef, newUserPromo);
    }

    private static void savePromotionToFirebase(DatabaseReference promotionsRef, Promotion promotion) {
        promotionsRef.child(promotion.getId()).setValue(promotion)
                .addOnSuccessListener(aVoid -> Log.d(TAG, promotion.getTitle() + " promotion saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save " + promotion.getTitle() + " promotion", e));
    }
}
