package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Objects;
import java.util.UUID;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.menu.PromotionFragment;

public class PromotionHelper {

    private static final String PROMOTIONS_NODE = "Promotions";
    private static final String USERS_NODE = "users";
    private static final String PROMOTIONS_FLAG_NODE = "promotions_added";
    private static final String TAG = "PromotionHelper";

    private PromotionHelper() {
    }

    public static void saveHardcodedPromotionsToFirebase() {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        DatabaseReference promotionsFlagRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_FLAG_NODE);

        promotionsFlagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class))) {
                    Log.d(TAG, "Promotions already added. Skipping...");
                } else {
                    addHardcodedPromotion(promotionsRef);
                    promotionsFlagRef.setValue(true)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Promotions flag set"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to set promotions flag", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to check promotions flag: ", databaseError.toException());
            }
        });
    }

    private static void addHardcodedPromotion(DatabaseReference promotionsRef) {
        Promotion promotion = new Promotion(
                promotionsRef.push().getKey(),
                "Exclusive Welcome Deal!",
                "Your journey begins with a reward—100% off just for signing up!",
                100
        );
        promotion.setPromoCode(UUID.randomUUID().toString().substring(0, 8));
        promotion.setUsed(false);

        promotionsRef.child(promotion.getId()).setValue(promotion)
                .addOnSuccessListener(aVoid -> Log.d(TAG, promotion.getTitle() + " saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save promotion", e));
    }

    public static void copyPromotionsToUsers() {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(USERS_NODE);

        promotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot promotionsSnapshot) {
                for (DataSnapshot promotionSnapshot : promotionsSnapshot.getChildren()) {
                    Promotion promotion = promotionSnapshot.getValue(Promotion.class);
                    if (promotion != null) {
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                                for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                    addPromotionToUser(userSnapshot.getKey(), promotion);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Failed to copy promotions to users: ", databaseError.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read promotions: ", databaseError.toException());
            }
        });
    }

    private static void addPromotionToUser(String uid, Promotion promotion) {
        if (uid == null) return;

        DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance()
                .getReference(USERS_NODE).child(uid).child("promotions").child(promotion.getId());

        userPromotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userPromotionSnapshot) {
                if (!userPromotionSnapshot.exists()) {
                    userPromotionsRef.setValue(promotion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to add promotion to user: ", databaseError.toException());
            }
        });
    }

    public static void applyPromoCode(String promoCode, PromoCallback callback) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance().getReference(USERS_NODE).child(uid).child("promotions");

        userPromotionsRef.orderByChild("promoCode").equalTo(promoCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    if (promotion != null && !promotion.isUsed()) {
                        callback.onSuccess(promotion.getDiscount());
                        return;
                    }
                }
                callback.onFailure("Invalid or already used promo code.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure("Failed to validate promo code.");
            }
        });
    }

    public static void setupPromoCodeEditText(EditText promoCodeEditText, Context context) {
        promoCodeEditText.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    promoCodeEditText.setText(clip.getItemAt(0).getText());
                    promoCodeEditText.setSelection(promoCodeEditText.getText().length());
                    Toast.makeText(context, "Promo code copied.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void markPromoCodeAsUsed(String promoCode, Context context) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance().getReference(USERS_NODE).child(uid).child("promotions");

        userPromotionsRef.orderByChild("promoCode").equalTo(promoCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    if (promotion != null && !promotion.isUsed()) {
                        promotion.setUsed(true);
                        userPromotionsRef.child(promotion.getId()).setValue(promotion)
                                .addOnSuccessListener(aVoid -> notifyPromotionFragment(context));
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to validate promo code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void notifyPromotionFragment(Context context) {
        if (context instanceof AppCompatActivity) {
            Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.flFragment);
            if (fragment instanceof PromotionFragment) {
                ((PromotionFragment) fragment).fetchPromotions();
            }
        }
    }

    public interface PromoCallback {
        void onSuccess(double discountAmount);
        void onFailure(String errorMessage);
    }
}
