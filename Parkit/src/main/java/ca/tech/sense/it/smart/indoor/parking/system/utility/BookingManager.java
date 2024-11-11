package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import java.util.concurrent.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BookingManager {

    // ExecutorService to manage a pool of threads
    private final ExecutorService executorService;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;
    private final ScheduledExecutorService scheduler;
    private final Context context;

    // Constructor with dependency injection
    public BookingManager(ExecutorService executorService, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth, Context context) {
        this.executorService = executorService;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.context = context;
    }

    // Callback interface for fetching parking location
    public interface FetchLocationCallback {
        void onFetchSuccess(ParkingLocation location);
        void onFetchFailure(Exception exception);
    }

    // Method to fetch parking location data from Firebase
    public void fetchParkingLocation(String locationId, FetchLocationCallback callback) {
        executorService.submit(() -> {
            DatabaseReference locationRef = firebaseDatabase.getReference("parkingLocations").child(locationId);
            locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ParkingLocation location = snapshot.getValue(ParkingLocation.class);
                    if (location != null) {
                        callback.onFetchSuccess(location);
                    } else {
                        callback.onFetchFailure(new Exception("Location data is not available"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onFetchFailure(error.toException());
                }
            });
        });
    }

    // Method to fetch the price of a parking location from Firebase
    public void fetchPrice(String locationId, Consumer<Double> onSuccess) {
        executorService.submit(() -> {
            DatabaseReference priceRef = firebaseDatabase.getReference("parkingLocations").child(locationId).child("price");
            priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Double priceValue = snapshot.getValue(Double.class);
                        double price = (priceValue != null) ? priceValue : 0.0;
                        onSuccess.accept(price);
                    } else {
                        onSuccess.accept(0.0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    onSuccess.accept(0.0);
                }
            });
        });
    }

    // Method to confirm a booking and save it to Firebase
    public void confirmBooking(String locationId, String slot, String timing, String selectedDate, String address, Runnable onSuccess, Consumer<Exception> onFailure) {
        // Add a delay of 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            executorService.submit(() -> {
                String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                String[] times = timing.split(" - ");
                long startTime = convertToMillis(selectedDate + " " + times[0]);
                long endTime = convertToMillis(selectedDate + " " + times[1]);

                DatabaseReference slotRef = firebaseDatabase.getReference("parkingLocations")
                        .child(locationId)
                        .child("slots")
                        .child(slot)
                        .child("hourlyStatus")
                        .child(selectedDate + " " + times[0]);

                slotRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.child("status").getValue(String.class);
                        if ("occupied".equals(status)) {
                            // Slot is occupied, notify the user
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(context, "Selected slot is already occupied. Please choose a different time slot.", Toast.LENGTH_SHORT).show()
                            );
                            onFailure.accept(new Exception("Selected slot is already occupied."));
                        } else {
                            // Slot is available, proceed with booking
                            DatabaseReference priceRef = firebaseDatabase.getReference("parkingLocations").child(locationId).child("price");
                            priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Double priceValue = snapshot.getValue(Double.class);
                                        double price = (priceValue != null) ? priceValue : 0.0;

                                        Booking booking = new Booking(
                                                "Park It", // Use "Park It" as title
                                                startTime,
                                                endTime,
                                                address,
                                                slot,
                                                price
                                        );

                                        DatabaseReference databaseRef = firebaseDatabase
                                                .getReference("users")
                                                .child(userId)
                                                .child("bookings")
                                                .push();

                                        databaseRef.setValue(booking)
                                                .addOnSuccessListener(aVoid -> {
                                                    updateHourlyStatus(locationId, slot, selectedDate, times[0], "occupied", () -> {
                                                        scheduleStatusUpdate(locationId, slot, selectedDate, times[1], onSuccess, onFailure);
                                                        // Show toast message
                                                        Toast.makeText(context, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                                                    }, onFailure);
                                                })
                                                .addOnFailureListener(onFailure::accept);
                                    } else {
                                        onFailure.accept(new Exception("Price not found for location: " + locationId));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    onFailure.accept(error.toException());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        onFailure.accept(error.toException());
                    }
                });
            });
        }, 2000); // 2 seconds delay
    }

    // Method to update the hourly status of a parking slot
    private void updateHourlyStatus(String locationId, String slot, String date, String hour, String status, Runnable onSuccess, Consumer<Exception> onFailure) {
        executorService.submit(() -> {
            DatabaseReference slotRef = firebaseDatabase.getReference("parkingLocations")
                    .child(locationId)
                    .child("slots")
                    .child(slot)
                    .child("hourlyStatus")
                    .child(date + " " + hour);

            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("bookingDate", date);
            statusUpdate.put("status", status);

            slotRef.updateChildren(statusUpdate)
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(onFailure::accept);
        });
    }

    // Method to schedule the status update to "available" after the booking time ends
    private void scheduleStatusUpdate(String locationId, String slot, String date, String hour, Runnable onSuccess, Consumer<Exception> onFailure) {
        long delay = calculateDelay(date + " " + hour);
        scheduler.schedule(() -> {
            updateHourlyStatus(locationId, slot, date, hour, "available", onSuccess, onFailure);
        }, delay, TimeUnit.MILLISECONDS);
    }

    // Method to save a parking location to the user's favorites in Firebase
    public void saveLocationToFavorites(String locationId, String address, Runnable onSuccess, Consumer<Exception> onFailure) {
        executorService.submit(() -> {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("locationId", locationId);
            locationData.put("address", address);

            DatabaseReference databaseRef = firebaseDatabase.getReference("users").child(userId).child("saved_locations").child(locationId);

            databaseRef.setValue(locationData).addOnSuccessListener(aVoid -> onSuccess.run()).addOnFailureListener(onFailure::accept);
        });
    }

    // Helper method to convert date and time to milliseconds
    private long convertToMillis(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Helper method to calculate the delay for scheduling the status update
    private long calculateDelay(String dateTime) {
        long endTimeMillis = convertToMillis(dateTime);
        long currentTimeMillis = System.currentTimeMillis();
        return endTimeMillis - currentTimeMillis;
    }
}
