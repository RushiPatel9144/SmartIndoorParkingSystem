package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.content.Context;
import android.content.Intent;
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

import ca.tech.sense.it.smart.indoor.parking.system.booking.ParkingTicket;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
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

    public void confirmBooking(String transactionId, String timing, String selectedDate, Booking booking, Runnable onSuccess, Consumer<Exception> onFailure) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> executorService.submit(() -> {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
            String[] times = timing.split(" - ");
            long startTime = BookingUtils.convertToMillis(selectedDate + " " + times[0]);
            long endTime = BookingUtils.convertToMillis(selectedDate + " " + times[1]);

            slotService.checkSlotAvailability(booking.getLocationId(), booking.getSlotNumber(), selectedDate, times[0], status -> {
                if ("occupied".equals(status)) {
                    notifyUserSlotOccupied(onFailure);
                } else {
                    fetchPriceAndConfirmBooking(transactionId, selectedDate, times, startTime, endTime, userId, booking, onSuccess, onFailure);
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

    private void fetchPriceAndConfirmBooking(String transactionId, String selectedDate, String[] times, long startTime, long endTime, String userId,Booking details, Runnable onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference priceRef = firebaseDatabase.getReference("parkingLocations").child(details.getLocationId()).child("price");
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
                            details.getTitle(),
                            startTime,
                            endTime,
                            details.getLocation(),
                            null,
                            totalPrice, // Use total price instead of just price
                            null,
                            null,
                            details.getSlotNumber(),
                            passKey,
                            details.getLocationId(), // Add the locationId to the booking
                            transactionId
                    );

                    saveBooking(userId, booking, details.getLocationId(), details.getSlotNumber(), selectedDate, times, onSuccess, onFailure);
                } else {
                    onFailure.accept(new Exception("Price not found for location: " + details.getLocationId()));
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
                        // Pass the booking details, including the pass key, to the ParkingTicketActivity
                        Intent intent = new Intent(context, ParkingTicket.class);
                        intent.putExtra("booking", booking); // Pass the entire booking object
                        context.startActivity(intent);
                    }, onFailure))
                    .addOnFailureListener(onFailure::accept);
        } else {
            onFailure.accept(new Exception("Failed to generate booking ID"));
        }
    }
}
