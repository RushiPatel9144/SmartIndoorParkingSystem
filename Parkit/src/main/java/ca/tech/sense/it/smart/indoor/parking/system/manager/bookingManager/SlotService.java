package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingUtils;

public class SlotService {

    private final ExecutorService executorService;
    private final FirebaseDatabase firebaseDatabase;
    private final ScheduledExecutorService scheduler;

    public SlotService(ExecutorService executorService, FirebaseDatabase firebaseDatabase, ScheduledExecutorService scheduler) {
        this.executorService = executorService;
        this.firebaseDatabase = firebaseDatabase;
        this.scheduler = scheduler;
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

    public void checkSlotAvailability(String locationId, String slot, String selectedDate, String time, Consumer<String> onStatusChecked, Consumer<Exception> onFailure) {
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

    public void updateHourlyStatus(String locationId, String slot, String date, String hour, String status, Runnable onSuccess, Consumer<Exception> onFailure) {
        executorService.submit(() -> {
            // Sanitize the slot ID to remove invalid characters
            String sanitizedSlot = sanitizeSlotId(slot);

            DatabaseReference slotRef = firebaseDatabase.getReference("parkingLocations")
                    .child(locationId)
                    .child("slots")
                    .child(sanitizedSlot)  // Use sanitized slot ID
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

    // Helper method to sanitize slot IDs
    private String sanitizeSlotId(String slotId) {
        return slotId.replaceAll("[.#$\\[\\]]", "_"); // Replace invalid characters with '_'
    }


    public void scheduleStatusUpdate(String locationId, String slot, String date, String hour, Runnable onSuccess, Consumer<Exception> onFailure) {
        long delay = BookingUtils.calculateDelay(date + " " + hour);
        scheduler.schedule(() -> updateHourlyStatus(locationId, slot, date, hour, "available", onSuccess, onFailure), delay, TimeUnit.MILLISECONDS);
    }

    public void updateSlotStatusToAvailable(String locationId, String slot, long startTime, long endTime, Runnable onSuccess, Consumer<Exception> onFailure) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String startDateTime = sdf.format(new Date(startTime));
        String endDateTime = sdf.format(new Date(endTime));

        DatabaseReference slotRef = firebaseDatabase.getReference("parkingLocations")
                .child(locationId)
                .child("slots")
                .child(slot)
                .child("hourlyStatus");

        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put(startDateTime + "/status", "available");
        statusUpdate.put(endDateTime + "/status", "available");

        slotRef.updateChildren(statusUpdate)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }
}
