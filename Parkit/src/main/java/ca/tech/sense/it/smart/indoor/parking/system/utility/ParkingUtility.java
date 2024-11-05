package ca.tech.sense.it.smart.indoor.parking.system.utility;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSensor;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import java.util.HashMap;
import java.util.Map;

public class ParkingUtility {

    private final DatabaseReference databaseReference;

    public ParkingUtility() {
        databaseReference = FirebaseDatabase.getInstance().getReference("parkingLocations");
    }

    // Add a new parking location
    public void addParkingLocation(ParkingLocation location) {
        String locationId = databaseReference.push().getKey(); // Generate a unique ID
        if (locationId != null) {
            location.setId(locationId); // Set the ID to the ParkingLocation object
            databaseReference.child(locationId).setValue(location)
                    .addOnSuccessListener(aVoid -> {
                        // Data saved successfully
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save data
                    });
        }
    }

    // Add a slot to an existing parking location
    public void addSlotToLocation(String locationId, ParkingSlot slot) {
        DatabaseReference slotsRef = databaseReference.child(locationId).child("slots");
        String slotId = slotsRef.push().getKey(); // Generate a unique ID for the slot
        if (slotId != null) {
            slot.setId(slotId); // Set the ID to the ParkingSlot object
            slotsRef.child(slotId).setValue(slot)
                    .addOnSuccessListener(aVoid -> {
                        // Slot added successfully
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save data
                    });
        }
    }

    // Add or update a sensor in a specific parking slot
    public void addOrUpdateSensorInSlot(String locationId, String slotId, ParkingSensor sensor) {
        DatabaseReference sensorRef = databaseReference.child(locationId).child("slots").child(slotId).child("sensor");
        sensorRef.setValue(sensor)
                .addOnSuccessListener(aVoid -> {
                    // Sensor added/updated successfully
                })
                .addOnFailureListener(e -> {
                    // Failed to save data
                });
    }

    // Fetch all parking locations
    public void fetchAllParkingLocations(final FetchLocationsCallback callback) {
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
                callback.onFetchSuccess(locations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFetchFailure(databaseError.toException());
            }
        });
    }

    // Fetch a specific parking location by ID
    public void fetchParkingLocation(String locationId, final FetchLocationCallback callback) {
        databaseReference.child(locationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ParkingLocation location = dataSnapshot.getValue(ParkingLocation.class);
                if (location != null) {
                    location.setId(locationId); // Set the ID from Firebase
                    callback.onFetchSuccess(location);
                } else {
                    callback.onFetchFailure(new Exception(String.valueOf(R.string.location_not_found)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFetchFailure(databaseError.toException());
            }
        });
    }

    // Fetch a parking location from Firestore by document ID
    public void fetchParkingLocationById(String id, FetchLocationCallback callback) {
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

    // Fetch slots for a specific parking location
    public void fetchSlotsForLocation(String locationId, final FetchSlotsCallback callback) {
        databaseReference.child(locationId).child("slots").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, ParkingSlot> slots = new HashMap<>();
                for (DataSnapshot slotSnapshot : dataSnapshot.getChildren()) {
                    ParkingSlot slot = slotSnapshot.getValue(ParkingSlot.class);
                    if (slot != null) {
                        slot.setId(slotSnapshot.getKey()); // Set the ID from Firebase
                        slots.put(slotSnapshot.getKey(), slot);
                    }
                }
                callback.onFetchSuccess(slots);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFetchFailure(databaseError.toException());
            }
        });
    }

    // Interfaces for callback
    public interface FetchLocationsCallback {
        void onFetchSuccess(Map<String, ParkingLocation> locations);
        void onFetchFailure(Exception exception);
    }

    public interface FetchLocationCallback {
        void onFetchSuccess(ParkingLocation location);
        void onFetchFailure(Exception exception);
    }

    public interface FetchSlotsCallback {
        void onFetchSuccess(Map<String, ParkingSlot> slots);
        void onFetchFailure(Exception exception);
    }

    // Method to get sensor data for a specific parking location and slot
    public void getSensorData(String locationId, String slotId, final FetchSensorDataCallback callback) {
        DatabaseReference sensorRef = databaseReference.child(locationId).child("slots").child(slotId).child("sensor");
        sensorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ParkingSensor sensor = dataSnapshot.getValue(ParkingSensor.class);
                if (sensor != null) {
                    callback.onFetchSuccess(sensor);
                } else {
                    callback.onFetchFailure(new Exception("Sensor data not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFetchFailure(databaseError.toException());
            }
        });
    }

    // Interface for fetching sensor data callback
    public interface FetchSensorDataCallback {
        void onFetchSuccess(ParkingSensor sensor);
        void onFetchFailure(Exception exception);
    }

}
