package ca.tech.sense.it.smart.indoor.parking.system.booking;

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
import java.util.Random;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.R;
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

    public void confirmBooking(String locationId, String slot, String timing, String selectedDate, String address, Runnable onSuccess, Consumer<Exception> onFailure) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            executorService.submit(() -> {
                String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                String[] times = timing.split(" - ");
                long startTime = convertToMillis(selectedDate + " " + times[0]);
                long endTime = convertToMillis(selectedDate + " " + times[1]);

                checkSlotAvailability(locationId, slot, selectedDate, times[0], status -> {
                    if ("occupied".equals(status)) {
                        notifyUserSlotOccupied(onFailure);
                    } else {
                        fetchPriceAndConfirmBooking(locationId, slot, selectedDate, times, startTime, endTime, address, userId, onSuccess, onFailure);
                    }
                }, onFailure);
            });
        }, 2000); // 2 seconds delay
    }

    private void checkSlotAvailability(String locationId, String slot, String selectedDate, String time, Consumer<String> onStatusChecked, Consumer<Exception> onFailure) {
        DatabaseReference slotRef = firebaseDatabase.getReference("parkingLocations")
                .child(locationId)
                .child("slots")
                .child(slot)
                .child("hourlyStatus")
                .child(selectedDate + " " + time);

        slotRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);
                onStatusChecked.accept(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onFailure.accept(error.toException());
            }
        });
    }

    private void notifyUserSlotOccupied(Consumer<Exception> onFailure) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "Selected slot is already occupied. Please choose a different time slot.", Toast.LENGTH_SHORT).show()
        );
        onFailure.accept(new Exception("Selected slot is already occupied."));
    }

    private void fetchPriceAndConfirmBooking(String locationId, String slot, String selectedDate, String[] times, long startTime, long endTime, String address, String userId, Runnable onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference priceRef = firebaseDatabase.getReference("parkingLocations").child(locationId).child("price");
        priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double priceValue = snapshot.getValue(Double.class);
                    double price = (priceValue != null) ? priceValue : 0.0;

                    String passKey = generatePassKey(); // Generate the pass key

                    Booking booking = new Booking(
                            "Park It", // Use "Park It" as title
                            startTime,
                            endTime,
                            address,
                            slot,
                            price,
                            passKey // Add the pass key to the booking
                    );

                    saveBooking(userId, booking, locationId, slot, selectedDate, times, onSuccess, onFailure);
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

    private void saveBooking(String userId, Booking booking, String locationId, String slot, String selectedDate, String[] times, Runnable onSuccess, Consumer<Exception> onFailure) {
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
    }

    // Method to generate a 4-digit pass key
    private String generatePassKey() {
        Random random = new Random();
        int passKey = 1000 + random.nextInt(9000); // Generates a random 4-digit number
        return String.valueOf(passKey);
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
    public void saveLocationToFavorites(String locationId, String address, String postalCode, Runnable onSuccess, Consumer<Exception> onFailure) {
        executorService.submit(() -> {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("locationId", locationId);
            locationData.put("address", address);
            locationData.put("postalCode", postalCode); // Add postal code to the data

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
                    Toast.makeText(context, "Failed to expire pass key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}