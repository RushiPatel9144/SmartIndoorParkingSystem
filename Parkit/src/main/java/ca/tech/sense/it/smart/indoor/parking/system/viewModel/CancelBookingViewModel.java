package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.booking.BookingManager;

public class CancelBookingViewModel extends AndroidViewModel {
    private final BookingManager bookingManager;

    public CancelBookingViewModel(@NonNull Application application) {
        super(application);
        bookingManager = new BookingManager(Executors.newSingleThreadExecutor(), FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), application.getApplicationContext());
    }

    public void cancelBooking(String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        bookingManager.cancelBooking(userId, bookingId, onSuccess, onFailure);
    }

    public void clearBookingHistory(String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        bookingManager.clearBookingHistory(userId, bookingId, onSuccess, onFailure);
    }

    public void clearAllBookingHistory(Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        bookingManager.clearAllBookingHistory(userId, onSuccess, onFailure);
    }
}


