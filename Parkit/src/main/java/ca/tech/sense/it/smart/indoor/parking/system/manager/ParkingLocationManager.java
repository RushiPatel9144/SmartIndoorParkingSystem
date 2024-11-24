package ca.tech.sense.it.smart.indoor.parking.system.manager;

import static ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.ToastHelper.showToast;
import static com.android.volley.VolleyLog.TAG;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingInterface;

public class ParkingLocationManager {
    private final DatabaseReference databaseReference;
    private final DatabaseReference ownerReference;
    private static final String COLLECTION_LOCATION_OWNER = "parkingLocationIds";

    public ParkingLocationManager() {
        databaseReference = FirebaseDatabaseSingleton.getInstance().getReference("parkingLocations");
        ownerReference = FirebaseDatabaseSingleton.getInstance().getReference("owners");
    }

    public void addParkingLocation(Context context, String ownerId, ParkingLocation location) {
        String locationId = databaseReference.push().getKey(); // Generate a unique ID
        location.setId(locationId); // Set the ID to the ParkingLocation object

        assert locationId != null;
        databaseReference.child(locationId).setValue(location)
                .addOnSuccessListener(aVoid -> {
                    DatabaseReference ownersRef = ownerReference.child(ownerId).child(COLLECTION_LOCATION_OWNER);
                    ownersRef.child(locationId).setValue(location)
                            .addOnSuccessListener(aVoid1 ->
                                    showToast(context, context.getString(R.string.parking_location_added_successfully)))
                            .addOnFailureListener(e ->
                                    Log.e("DatabaseError", "Failed to add location ID to owner's collection: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        Log.e("DatabaseError", "Failed to add parking location: " + e.getMessage()));
    }

    public void deleteParkingLocation(Context context, String ownerId, String locationId) {
        // References to the parking location in the main database and owner's collection
        DatabaseReference locationRef = databaseReference.child(locationId);
        DatabaseReference ownerLocationRef = ownerReference.child(ownerId).child(COLLECTION_LOCATION_OWNER).child(locationId);

        // First delete from the main database
        locationRef.removeValue()
                .addOnSuccessListener(aVoid ->
                    // Proceed to delete from owner's collection
                    ownerLocationRef.removeValue()
                            .addOnSuccessListener(aVoid1 ->
                                // Both deletions were successful
                                showToast(context, context.getString(R.string.parking_location_deleted_successfully)))
                            .addOnFailureListener(e -> {
                                // Rollback: Re-add the location back to the main database
                                locationRef.setValue(locationId)
                                        .addOnFailureListener(e2 -> Log.e("DatabaseError", "Failed to rollback main location: " + e2.getMessage()));
                                showToast(context, context.getString(R.string.failed_to_delete_owner_parking_location));
                            }))
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Failed to delete parking location: " + e.getMessage());
                    showToast(context, context.getString(R.string.failed_to_delete_parking_location));
                });
    }

    public void fetchAllParkingLocations(final ParkingInterface.FetchLocationsCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, ParkingLocation> locations = new HashMap<>();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    ParkingLocation location = locationSnapshot.getValue(ParkingLocation.class);
                    if (location != null) {
                        location.setId(locationSnapshot.getKey()); // Set the ID from Firebase
                        locations.put(locationSnapshot.getKey(), location);
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> callback.onFetchSuccess(locations));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFetchFailure(databaseError.toException()));
            }
        });
    }

    // Method to fetch parking location data from Firebase
    public void fetchParkingLocation(Context context, ExecutorService executorService, String locationId, ParkingInterface.FetchLocationCallback callback) {
        executorService.submit(() -> {
            DatabaseReference locationRef = databaseReference.child(locationId);
            locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ParkingLocation location = snapshot.getValue(ParkingLocation.class);
                    if (location != null) {
                        callback.onFetchSuccess(location);
                    } else {
                        callback.onFetchFailure(new Exception(context.getString(R.string.location_data_is_not_available)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onFetchFailure(error.toException());
                }
            });
        });
    }

    public void fetchParkingLocationById(String id, ParkingInterface.FetchLocationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("parkingLocations").document(id);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ParkingLocation location = document.toObject(ParkingLocation.class);
                    callback.onFetchSuccess(location);
                } else {
                    callback.onFetchFailure(new Exception("Document does not exist"));
                }
            } else {
                callback.onFetchFailure(task.getException());
            }
        });
    }

    public void fetchParkingLocationsByOwnerId(String ownerId, ParkingInterface.ParkingLocationFetchCallback callback) {
        DatabaseReference db = ownerReference.child(ownerId).child(COLLECTION_LOCATION_OWNER);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ParkingLocation> fetchedLocations = new ArrayList<>();
                for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                    ParkingLocation location = locationSnapshot.getValue(ParkingLocation.class);
                    if (location != null) {
                        fetchedLocations.add(location);
                    }
                }
                callback.onFetchSuccess(fetchedLocations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFetchFailure(error.getMessage());
            }
        });
    }

    public void changePrice(@NonNull Context context, @NonNull String ownerId,
                            @NonNull String locationId, double newPrice) {
        // Get the reference to the parking location
        DatabaseReference locationRef = databaseReference.child(locationId);
        // Update the price of the parking location
        locationRef.child("price").setValue(newPrice)
                .addOnSuccessListener(aVoid -> {
                    // Optionally update the price in the owner's collection as well
                    DatabaseReference ownerLocationRef = ownerReference.child(ownerId)
                            .child(COLLECTION_LOCATION_OWNER).child(locationId);
                    ownerLocationRef.child("price").setValue(newPrice)
                            .addOnSuccessListener(aVoid1 ->
                                showToast(context, context.getString(R.string.price_updated_successfully)))
                            .addOnFailureListener(e ->
                                Log.e(TAG, "Failed to update price in owner's collection: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update price: " + e.getMessage());
                    showToast(context, context.getString(R.string.error_updating_price));
                });
    }
}
