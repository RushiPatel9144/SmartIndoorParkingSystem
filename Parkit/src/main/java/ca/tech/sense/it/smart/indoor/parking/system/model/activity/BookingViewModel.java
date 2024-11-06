package ca.tech.sense.it.smart.indoor.parking.system.model.activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookingViewModel extends ViewModel {
    private final MutableLiveData<List<Booking>> activeBookings = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> historyBookings = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> upcomingBookings = new MutableLiveData<>();

    public BookingViewModel() {
        fetchBookings();
    }

    public LiveData<List<Booking>> getActiveBookings() {
        return activeBookings;
    }

    public LiveData<List<Booking>> getHistoryBookings() {
        return historyBookings;
    }

    public LiveData<List<Booking>> getUpcomingBookings() {
        return upcomingBookings;
    }

    private void fetchBookings() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("bookings");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> activeList = new ArrayList<>();
                List<Booking> historyList = new ArrayList<>();
                List<Booking> upcomingList = new ArrayList<>();

                long currentTime = System.currentTimeMillis();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot bookingSnapshot : userSnapshot.getChildren()) {
                        Booking booking = bookingSnapshot.getValue(Booking.class);
                        if (booking != null) {
                            if (booking.getEndTime() < currentTime) {
                                historyList.add(booking);
                            } else if (booking.getStartTime() <= currentTime && booking.getEndTime() >= currentTime) {
                                activeList.add(booking);
                            } else {
                                upcomingList.add(booking);
                            }
                        }
                    }
                }

                activeBookings.setValue(activeList);
                historyBookings.setValue(historyList);
                upcomingBookings.setValue(upcomingList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }
}
