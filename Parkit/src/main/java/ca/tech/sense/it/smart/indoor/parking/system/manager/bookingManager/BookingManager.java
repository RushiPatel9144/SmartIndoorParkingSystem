package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

/**
 * Manages booking, slot, and user services.
 */
public class BookingManager {

    private final BookingService bookingService;
    private final SlotService slotService;
    private final UserService userService;

    /**
     * Initializes services.
     */
    public BookingManager(ExecutorService executorService, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth, Context context) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        this.slotService = new SlotService(executorService, firebaseDatabase, scheduler,context);
        this.bookingService = new BookingService(executorService, firebaseDatabase, firebaseAuth, context, slotService);
        this.userService = new UserService(executorService, firebaseDatabase, firebaseAuth);
    }

    /**
     * @return BookingService instance.
     */
    public BookingService getBookingService() {
        return bookingService;
    }

    /**
     * @return SlotService instance.
     */
    public SlotService getSlotService() {
        return slotService;
    }

    /**
     * @return UserService instance.
     */
    public UserService getUserService() {
        return userService;
    }

    public void getUserBookings(String userId, final Consumer<List<Booking>> callback) {
        DatabaseReference userBookingsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("bookings");

        userBookingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> bookings = new ArrayList<>();
                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    Booking booking = bookingSnap.getValue(Booking.class);
                    if (booking != null) bookings.add(booking);
                }
                callback.accept(bookings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.accept(new ArrayList<>());
            }
        });
    }

}
