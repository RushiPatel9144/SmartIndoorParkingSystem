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
import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingUtils;

import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;

public class CancelBookingViewModel extends AndroidViewModel {
    private final BookingManager bookingManager;

    public CancelBookingViewModel(@NonNull Application application) {
        super(application);
        bookingManager = new BookingManager(Executors.newSingleThreadExecutor(), FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), application.getApplicationContext());
    }

    public void cancelBooking(Booking booking, Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        bookingManager.getUserService().cancelBookingAndRequestRefund(userId, booking, () -> {
            // Send cancellation notification
            sendCancellationNotification(userId, booking);
            onSuccess.run();
        }, onFailure);
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

    public void sendCancellationNotification(String userId, Booking booking) {
        String title = "Booking Canceled";
        String message = "Your booking at " + booking.getLocation() + " for " + BookingUtils.formatDate(booking.getStartTime()) + " has been canceled.";
        NotificationHelper.sendNotification(getApplication().getApplicationContext(), title, message, userId);
    }
}

