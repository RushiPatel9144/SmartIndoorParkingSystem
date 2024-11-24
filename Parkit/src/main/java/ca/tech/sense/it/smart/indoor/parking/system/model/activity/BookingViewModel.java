/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.model.activity;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;

public class BookingViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Booking>> activeBookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> upcomingBookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> historyBookingsLiveData = new MutableLiveData<>();
    private final BookingManager bookingManager;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        bookingManager = new BookingManager(Executors.newSingleThreadExecutor(), FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), application.getApplicationContext());
    }

    public LiveData<List<Booking>> getActiveBookings() {
        return activeBookingsLiveData;
    }

    public LiveData<List<Booking>> getUpcomingBookings() {
        return upcomingBookingsLiveData;
    }

    public LiveData<List<Booking>> getHistoryBookings() {
        return historyBookingsLiveData;
    }

    public void fetchUserBookings() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bookings");

        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> activeBookings = new ArrayList<>();
                List<Booking> upcomingBookings = new ArrayList<>();
                List<Booking> historyBookings = new ArrayList<>();
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (booking != null) {
                        booking.setId(bookingSnapshot.getKey());
                        if (booking.getEndTime() < currentTime) {
                            historyBookings.add(booking);
                            bookingManager.getUserService().expirePassKey(userId, bookingSnapshot.getKey());
                        } else if (booking.getStartTime() > currentTime) {
                            upcomingBookings.add(booking);
                        } else {
                            activeBookings.add(booking);
                        }
                    }
                }

                // Sort upcoming bookings by start time
                upcomingBookings.sort(Comparator.comparingLong(Booking::getStartTime));

                activeBookingsLiveData.setValue(activeBookings);
                upcomingBookingsLiveData.setValue(upcomingBookings);
                historyBookingsLiveData.setValue(historyBookings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

}
