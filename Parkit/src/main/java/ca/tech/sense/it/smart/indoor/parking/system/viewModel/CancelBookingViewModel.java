package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

public class CancelBookingViewModel extends AndroidViewModel {
    private final BookingManager bookingManager;

    public CancelBookingViewModel(@NonNull Application application) {
        super(application);
        bookingManager = new BookingManager(Executors.newSingleThreadExecutor(), FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), application.getApplicationContext());
    }

    public void cancelBooking(String transactionId, String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        bookingManager.getUserService().cancelBookingAndRequestRefund(userId,bookingId, transactionId, onSuccess, onFailure);
    }

    public void clearAllBookingHistory(Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        bookingManager.getUserService().clearAllBookingHistory(userId, bookings -> {
            long currentTime = System.currentTimeMillis();
            for (Booking booking : bookings) {
                if (booking.getEndTime() < currentTime) {
                    bookingManager.getUserService().clearBookingHistory(userId, booking.getId(), onSuccess, onFailure);
                }
            }
            onSuccess.run();
        }, onFailure);
    }
}

