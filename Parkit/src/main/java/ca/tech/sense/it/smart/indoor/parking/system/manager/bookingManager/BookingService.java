package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingUtils;

public class BookingService {

    private final ExecutorService executorService;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;
    private final Context context;
    private final SlotService slotService;

    public BookingService(ExecutorService executorService, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth, Context context, SlotService slotService) {
        this.executorService = executorService;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.context = context;
        this.slotService = slotService;
    }

    public void confirmBooking(String locationId, String slot, String timing, String selectedDate, String address, Runnable onSuccess, Consumer<Exception> onFailure) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> executorService.submit(() -> {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
            String[] times = timing.split(" - ");
            long startTime = BookingUtils.convertToMillis(selectedDate + " " + times[0]);
            long endTime = BookingUtils.convertToMillis(selectedDate + " " + times[1]);

            slotService.checkSlotAvailability(locationId, slot, selectedDate, times[0], status -> {
                if ("occupied".equals(status)) {
                    notifyUserSlotOccupied(onFailure);
                } else {
                    fetchPriceAndConfirmBooking(locationId, slot, selectedDate, times, startTime, endTime, address, userId, onSuccess, onFailure);
                }
            }, onFailure);
        }), 2000); // 2 seconds delay
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

                    // Calculate total price including GST/HST and platform fee
                    double gstHst = price * 0.13;
                    double platformFee = price * 0.10;
                    double totalPrice = price + gstHst + platformFee;

                    String passKey = BookingUtils.generatePassKey(); // Generate the pass key

                    Booking booking = new Booking(
                            "Park It", // Use "Park It" as title
                            startTime,
                            endTime,
                            address,
                            null,
                            totalPrice, // Use total price instead of just price
                            null,
                            null,
                            slot,
                            passKey,
                            locationId // Add the locationId to the booking
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

        String bookingId = databaseRef.getKey();
        if (bookingId != null) {
            booking.setId(bookingId); // Set the booking ID
            databaseRef.setValue(booking)
                    .addOnSuccessListener(aVoid -> slotService.updateHourlyStatus(locationId, slot, selectedDate, times[0], "occupied", () -> {
                        slotService.scheduleStatusUpdate(locationId, slot, selectedDate, times[1], onSuccess, onFailure);
                        // Show toast message
                        Toast.makeText(context, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                    }, onFailure))
                    .addOnFailureListener(onFailure::accept);
        } else {
            onFailure.accept(new Exception("Failed to generate booking ID"));
        }
    }
}
