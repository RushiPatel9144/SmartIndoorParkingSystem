package ca.tech.sense.it.smart.indoor.parking.system.model.activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class BookingViewModel extends ViewModel {
    private final MutableLiveData<List<Booking>> upcomingBookings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Booking>> activeBookings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Booking>> historyBookings = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Booking>> getUpcomingBookings() {
        return upcomingBookings;
    }

    public LiveData<List<Booking>> getActiveBookings() {
        return activeBookings;
    }

    public LiveData<List<Booking>> getHistoryBookings() {
        return historyBookings;
    }

    public void addBooking(Booking booking) {
        List<Booking> currentUpcoming = upcomingBookings.getValue();
        if (currentUpcoming != null) {
            currentUpcoming.add(booking);
            upcomingBookings.setValue(currentUpcoming);
        }
    }

    public void moveBookingToActive(Booking booking) {
        List<Booking> currentUpcoming = upcomingBookings.getValue();
        List<Booking> currentActive = activeBookings.getValue();
        if (currentUpcoming != null && currentActive != null) {
            currentUpcoming.remove(booking);
            currentActive.add(booking);
            upcomingBookings.setValue(currentUpcoming);
            activeBookings.setValue(currentActive);
        }
    }

    public void moveBookingToHistory(Booking booking) {
        List<Booking> currentActive = activeBookings.getValue();
        List<Booking> currentHistory = historyBookings.getValue();
        if (currentActive != null && currentHistory != null) {
            currentActive.remove(booking);
            currentHistory.add(booking);
            activeBookings.setValue(currentActive);
            historyBookings.setValue(currentHistory);
        }
    }
}

