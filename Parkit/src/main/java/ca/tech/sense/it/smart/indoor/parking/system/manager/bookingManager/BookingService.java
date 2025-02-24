package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.booking.ParkingTicket;
import ca.tech.sense.it.smart.indoor.parking.system.manager.notificationManager.NotificationManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingUtils;
import android.app.AlarmManager;
import android.app.PendingIntent;

import androidx.annotation.NonNull;


public class BookingService {

    private final ExecutorService executorService;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;
    private final Context context;
    private final SlotService slotService;
    private ValueEventListener carParkedListener;
    private DatabaseReference carParkedRef;

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

            slotService.checkSlotAvailability(booking.getLocationId(), booking.getSlotNumber(), selectedDate, times[0], (status, carParked) -> {
                if ("occupied".equals(status)) {
                    notifyUserSlotOccupied(onFailure);
                } else {
                    fetchPriceAndConfirmBooking(transactionId, selectedDate, times, startTime, endTime, userId, booking, onSuccess, onFailure);
                }
            }, onFailure);
        }), 2000);
    }


    private void notifyUserSlotOccupied(Consumer<Exception> onFailure) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, context.getString(R.string.selected_slot_is_already_occupied_please_choose_a_different_time_slot), Toast.LENGTH_SHORT).show()
        );
        onFailure.accept(new Exception("Selected slot is already occupied."));
    }

    private void fetchPriceAndConfirmBooking(String transactionId, String selectedDate, String[] times, long startTime, long endTime, String userId, Booking details, Runnable onSuccess, Consumer<Exception> onFailure) {
        Booking booking = new Booking(null, details.getTitle(), startTime, endTime, details.getLocation(), null,
                details.getTotalPrice(), details.getPrice(), details.getCurrencyCode(), details.getCurrencySymbol(),
                details.getSlotNumber(), details.getPassKey(), details.getLocationId(), transactionId);

        saveBooking(userId, booking, details.getLocationId(), details.getSlotNumber(), selectedDate, times, onSuccess, onFailure);
    }

    private void saveBooking(String userId, Booking booking, String locationId, String slot, String selectedDate, String[] times, Runnable onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference databaseRef = firebaseDatabase.getReference("users").child(userId).child("bookings").push();
        String bookingId = databaseRef.getKey();
        booking.setId(bookingId);

        if (bookingId != null) {
            databaseRef.setValue(booking).addOnSuccessListener(aVoid -> {
                slotService.updateHourlyStatus(locationId, slot, selectedDate, times[0], "occupied", false, () -> {
                    slotService.scheduleStatusUpdate(locationId, slot, selectedDate, times[1], onSuccess, onFailure);
                    Toast.makeText(context, context.getString(R.string.booking_confirmed), Toast.LENGTH_SHORT).show();
                    monitorCarParkedStatus(locationId, slot, selectedDate, times[0]);
                    Intent intent = new Intent(context, ParkingTicket.class);
                    intent.putExtra("booking", booking);
                    context.startActivity(intent);
                    NotificationManagerHelper.sendBookingConfirmationNotification(userId, booking, selectedDate, times);
                    scheduleBookingReminders(userId, booking, selectedDate, times);
                }, onFailure);
            }).addOnFailureListener(onFailure::accept);
        } else {
            onFailure.accept(new Exception("Failed to generate booking ID"));
        }
    }

    private void monitorCarParkedStatus(String locationId, String slot, String selectedDate, String bookingTime) {
        carParkedRef = firebaseDatabase.getReference("parkingLocations").child(locationId).child("slots").child(slot).child("hourlyStatus").child(selectedDate + " " + bookingTime).child("carParked");

        if (carParkedListener != null) {
            carParkedRef.removeEventListener(carParkedListener);
        }

        carParkedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean carParked = snapshot.getValue(Boolean.class);
                    if (carParked != null) {
                        NotificationManagerHelper.sendCarStatusNotification(carParked);
                        Log.d("CarParkedStatus", "Car parked status updated: " + carParked);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CarParkedStatus", "Failed to read carParked status", error.toException());
            }
        };
        carParkedRef.addValueEventListener(carParkedListener);
    }

    public void updateTotalPrice(String userId, String bookingId, double totalPrice) {
        DatabaseReference databaseRef = firebaseDatabase
                .getReference("users")
                .child(userId)
                .child("bookings")
                .child(bookingId)
                .child("totalPrice");

        // Update the totalPrice with the new value
        databaseRef.setValue(totalPrice)
                .addOnSuccessListener(aVoid -> Log.d("BookingUpdate", "Total price updated successfully."))
                .addOnFailureListener(e -> Log.e("BookingUpdate", "Failed to update total price.", e));
    }

    private void scheduleBookingReminders(String userId, Booking booking, String selectedDate, String[] times) {
        long startTime = BookingUtils.convertToMillis(selectedDate + " " + times[0]);
        long reminderTime = startTime - 10 * 60 * 1000; // 30 minutes before

        scheduleReminder(reminderTime, context.getString(R.string.booking_reminder), context.getString(R.string.your_booking_at) + booking.getLocation() + context.getString(R.string.starts_in_30_minutes), userId);
        scheduleReminder(startTime,  context.getString(R.string.booking_reminder), context.getString(R.string.your_booking_at) + booking.getLocation() + context.getString(R.string.starts_now), userId);
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleReminder(long timeInMillis, String title, String message, String userId) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            // Do not schedule reminders if API level is greater than 31
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BookingReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("userId", userId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) timeInMillis, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }
}
