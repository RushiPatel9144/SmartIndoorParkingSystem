package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.ui.menu.PromotionFragment;

public class PromotionHelper {

    private static final String PROMOTIONS_NODE = "Promotions";
    private static final String TAG = "PromotionHelper";

    private PromotionHelper() {
    }

    public static void saveHardcodedPromotionsToFirebase() {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        DatabaseReference promotionsFlagRef = FirebaseDatabase.getInstance().getReference("promotions_added");

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
        Promotion newUserPromo = new Promotion(promotionsRef.push().getKey(), "New User", "Welcome to the app", 15);
        savePromotionToFirebase(promotionsRef, newUserPromo);
    }

    private static void savePromotionToFirebase(DatabaseReference promotionsRef, Promotion promotion) {
        promotion.setPromoCode(UUID.randomUUID().toString().substring(0, 8)); // Generate promo code
        promotion.setUsed(false); // Set the promo code as unused initially
        promotionsRef.child(promotion.getId()).setValue(promotion)
                .addOnSuccessListener(aVoid -> Log.d(TAG, promotion.getTitle() + " promotion saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save " + promotion.getTitle() + " promotion", e));
    }

    public static void copyPromotionsToUsers() {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

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
                                    String uid = userSnapshot.getKey();
                                    if (uid != null) {
                                        DatabaseReference userPromotionsRef = usersRef.child(uid).child("promotions").child(promotion.getId());
                                        userPromotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userPromotionSnapshot) {
                                                if (!userPromotionSnapshot.exists()) {
                                                    userPromotionsRef.setValue(promotion);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.e(TAG, "Failed to check user promotions: ", databaseError.toException());
                                            }
                                        });
                                    }
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

    public static void applyPromoCode(String promoCode, Booking booking, TextView subtotalTextView, TextView gstHstTextView, TextView platformFeeTextView, TextView totalTextView, Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("promotions");

        userPromotionsRef.orderByChild("promoCode").equalTo(promoCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isValidPromo = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    if (promotion != null && !promotion.isUsed()) {
                        isValidPromo = true;
                        double discount = promotion.getDiscount();
                        double subtotal = booking.getPrice();
                        double discountAmount = subtotal * (discount / 100);
                        double newSubtotal = subtotal - discountAmount;
                        double gstHst = newSubtotal * 0.13;
                        double platformFee = newSubtotal * 0.10;
                        double total = newSubtotal + gstHst + platformFee;

                        subtotalTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), newSubtotal));
                        gstHstTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), gstHst));
                        platformFeeTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), platformFee));
                        totalTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), total));

                        Toast.makeText(context, "Promo code applied successfully!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (!isValidPromo) {
                    Toast.makeText(context, "Invalid or already used promo code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to validate promo code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void setupPromoCodeEditText(EditText promoCodeEditText, Context context) {
        promoCodeEditText.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence pastedText = clip.getItemAt(0).getText();
                    promoCodeEditText.setText(pastedText);
                    promoCodeEditText.setSelection(promoCodeEditText.getText().length()); // Move cursor to the end
                    Toast.makeText(context, "Promo code copied. It will be applied at the time of payment.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void markPromoCodeAsUsed(String promoCode, Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("promotions");

        userPromotionsRef.orderByChild("promoCode").equalTo(promoCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AtomicBoolean promoUpdated = new AtomicBoolean(false); // Track if promo was successfully updated
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);

                    if (promotion != null && !promotion.isUsed()) {
                        promotion.setUsed(true);
                        userPromotionsRef.child(promotion.getId()).setValue(promotion)
                                .addOnSuccessListener(aVoid -> {
                                    promoUpdated.set(true);
                                    notifyPromotionFragment(context);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark promo code as used in Firebase", e));
                        break;
                    }
                }
                if (!promoUpdated.get()) {
                    Toast.makeText(context, "Invalid or already used promo code.", Toast.LENGTH_SHORT).show();
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
            AppCompatActivity activity = (AppCompatActivity) context;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.flFragment);
            if (fragment instanceof PromotionFragment) {
                ((PromotionFragment) fragment).fetchPromotions();
            }
        }
    }

    public static void removePromotionFromUser(String promoCode, Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userPromotionsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("promotions");

        userPromotionsRef.orderByChild("promoCode").equalTo(promoCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot promoSnapshot : dataSnapshot.getChildren()) {
                            promoSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Promotion removed from user's promotions.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove promotion from user's promotions.", Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "Failed to access user's promotions.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void syncPromotionsWithUsers() {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference(PROMOTIONS_NODE);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Listen for changes in the global Promotions node
        promotionsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Promotion promotion = dataSnapshot.getValue(Promotion.class);
                if (promotion != null) {
                    // Add the new promotion to each user's promotions node
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                            for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                if (uid != null) {
                                    DatabaseReference userPromotionsRef = usersRef.child(uid).child("promotions").child(promotion.getId());
                                    userPromotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userPromotionSnapshot) {
                                            if (!userPromotionSnapshot.exists()) {
                                                userPromotionsRef.setValue(promotion);
                                            } else {
                                                // Update promotion details except "used" flag
                                                Promotion userPromotion = userPromotionSnapshot.getValue(Promotion.class);
                                                if (userPromotion != null) {
                                                    userPromotion.setDescription(promotion.getDescription());
                                                    userPromotion.setDiscount(promotion.getDiscount());
                                                    userPromotion.setTitle(promotion.getTitle());
                                                    userPromotion.setPromoCode(promotion.getPromoCode());
                                                    userPromotionsRef.setValue(userPromotion);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e(TAG, "Failed to update promotion for users: ", databaseError.toException());
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to update promotion for users: ", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Promotion promotion = dataSnapshot.getValue(Promotion.class);
                if (promotion != null) {
                    // Update the promotion in each user's promotions node, except the "used" flag
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                            for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                if (uid != null) {
                                    DatabaseReference userPromotionsRef = usersRef.child(uid).child("promotions").child(promotion.getId());
                                    userPromotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userPromotionSnapshot) {
                                            if (userPromotionSnapshot.exists()) {
                                                Promotion userPromotion = userPromotionSnapshot.getValue(Promotion.class);
                                                if (userPromotion != null) {
                                                    userPromotion.setDescription(promotion.getDescription());
                                                    userPromotion.setDiscount(promotion.getDiscount());
                                                    userPromotion.setTitle(promotion.getTitle());
                                                    userPromotion.setPromoCode(promotion.getPromoCode());
                                                    userPromotionsRef.setValue(userPromotion);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e(TAG, "Failed to update promotion for users: ", databaseError.toException());
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to update promotion for users: ", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Promotion promotion = dataSnapshot.getValue(Promotion.class);
                if (promotion != null) {
                    // Remove the promotion from each user's promotions node
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                            for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                if (uid != null) {
                                    DatabaseReference userPromotionsRef = usersRef.child(uid).child("promotions").child(promotion.getId());
                                    userPromotionsRef.removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to remove promotion from users: ", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to listen for promotion changes: ", databaseError.toException());
            }
        });
    }
}
