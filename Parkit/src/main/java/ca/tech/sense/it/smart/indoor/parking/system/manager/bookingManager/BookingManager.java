package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
        this.slotService = new SlotService(executorService, firebaseDatabase, scheduler);
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
}
