/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.manager.favoriteManager;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;

public class FavoritesManager {

    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final ImageButton starButton;
    private final String locationId;
    private final TextView addressText;
    private final TextView postalCodeText;
    private final BookingManager bookingManager;

    public FavoritesManager(Context context, FirebaseAuth firebaseAuth, ImageButton starButton, String locationId,
                            TextView addressText, TextView postalCodeText, BookingManager bookingManager) {
        this.context = context;
        this.firebaseAuth = firebaseAuth;
        this.starButton = starButton;
        this.locationId = locationId;
        this.addressText = addressText;
        this.postalCodeText = postalCodeText;
        this.bookingManager = bookingManager;
    }

    public void setupStarButton() {
        if (firebaseAuth.getCurrentUser() == null) {
            showUserNotAuthenticatedMessage();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference userFavoritesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("saved_locations")
                .child(locationId);

        checkIfLocationInFavorites(userFavoritesRef);
        listenForFavoriteChanges(userFavoritesRef);

        starButton.setOnClickListener(v -> handleStarButtonClick(userFavoritesRef));
    }

    private void showUserNotAuthenticatedMessage() {
        Toast.makeText(context, R.string.user_not_authenticated, Toast.LENGTH_SHORT).show();
    }

    private void checkIfLocationInFavorites(DatabaseReference userFavoritesRef) {
        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setStarButtonToGreen();
                } else {
                    setStarButtonToBlack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check favorites" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForFavoriteChanges(DatabaseReference userFavoritesRef) {
        Objects.requireNonNull(userFavoritesRef.getParent()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (Objects.equals(snapshot.getKey(), locationId)) {
                    setStarButtonToGreen();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (Objects.equals(snapshot.getKey(), locationId)) {
                    setStarButtonToBlack();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to listen for changes" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStarButtonToGreen() {
        starButton.setColorFilter(ContextCompat.getColor(context, R.color.logo));
    }

    private void setStarButtonToBlack() {
        starButton.setColorFilter(ContextCompat.getColor(context, R.color.black));
    }

    private void handleStarButtonClick(DatabaseReference userFavoritesRef) {
        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    removeLocationFromFavorites(userFavoritesRef);
                } else {
                    addLocationToFavorites(userFavoritesRef);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check favorites" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeLocationFromFavorites(DatabaseReference userFavoritesRef) {
        userFavoritesRef.removeValue().addOnSuccessListener(aVoid -> {
            setStarButtonToBlack();
            Toast.makeText(context, "Location removed from favorites", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(error -> Toast.makeText(context, "Failed to remove location" + error.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addLocationToFavorites(DatabaseReference userFavoritesRef) {
        String address = addressText.getText().toString();
        String postalCode = postalCodeText.getText().toString();

        // Fetch the name from the database
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("parkingLocations").child(locationId);
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (name != null) {
                    saveLocationToFavorites(address, postalCode, name);
                } else {
                    Toast.makeText(context, "Failed to fetch the name", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to fetch the name" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocationToFavorites(String address, String postalCode, String name) {
        bookingManager.getUserService().saveLocationToFavorites(locationId, address, postalCode, name, () -> {
            setStarButtonToGreen();
            Toast.makeText(context, R.string.location_saved_to_favorites, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(context, context.getString(R.string.failed_to_save_location) + error.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

