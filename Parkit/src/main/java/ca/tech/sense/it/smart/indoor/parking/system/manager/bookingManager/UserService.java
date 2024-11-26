package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

public class UserService {

    private final ExecutorService executorService;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;

    public UserService(ExecutorService executorService, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth) {
        this.executorService = executorService;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
    }

    public void saveLocationToFavorites(String locationId, String address, String postalCode, String name, Runnable onSuccess, Consumer<Exception> onFailure) {
        executorService.submit(() -> {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("locationId", locationId);
            locationData.put("address", address);
            locationData.put("postalCode", postalCode);
            locationData.put("name", name); // Add name to the data

            DatabaseReference databaseRef = firebaseDatabase.getReference("users").child(userId).child("saved_locations").child(locationId);

            databaseRef.setValue(locationData).addOnSuccessListener(aVoid -> onSuccess.run()).addOnFailureListener(onFailure::accept);
        });
    }

    public void clearAllBookingHistory(String userId, Consumer<List<Booking>> onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference bookingsRef = firebaseDatabase.getReference("users").child(userId).child("bookings");
        bookingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> bookings = new ArrayList<>();
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
                onSuccess.accept(bookings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onFailure.accept(new Exception(error.getMessage()));
            }
        });
    }


    public void expirePassKey(String userId, String bookingId) {
        DatabaseReference bookingRef = firebaseDatabase.getReference("users")
                .child(userId)
                .child("bookings")
                .child(bookingId)
                .child("passKey");

        bookingRef.setValue(null) // Remove the pass key
                .addOnSuccessListener(aVoid -> {
                    // Pass key expired successfully
                    // Toast.makeText(context, "Pass key expired.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(null, "Failed to expire pass key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void cancelBooking(String userId, String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        if (bookingId == null) {
            onFailure.accept(new Exception("Booking ID is null"));
            return;
        }

        DatabaseReference bookingRef = firebaseDatabase.getReference("users").child(userId).child("bookings").child(bookingId);
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    // Remove the booking
                    bookingRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Update the slot status to "available"
                                SlotService slotService = new SlotService(executorService, firebaseDatabase, Executors.newScheduledThreadPool(1));
                                slotService.updateSlotStatusToAvailable(booking.getLocationId(), booking.getSlotNumber(), booking.getStartTime(), booking.getEndTime(), onSuccess, onFailure);
                            })
                            .addOnFailureListener(onFailure::accept);
                } else {
                    onFailure.accept(new Exception("Booking not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onFailure.accept(error.toException());
            }
        });
    }

    public void clearBookingHistory(String userId, String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        if (bookingId == null) {
            onFailure.accept(new Exception("Booking ID is null"));
            return;
        }

        DatabaseReference bookingRef = firebaseDatabase.getReference("users").child(userId).child("bookings").child(bookingId);
        bookingRef.removeValue()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }
}

