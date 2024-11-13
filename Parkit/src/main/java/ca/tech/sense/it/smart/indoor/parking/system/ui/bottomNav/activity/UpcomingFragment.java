/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.BookingViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.BookingAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.CancelBookingViewModel;

public class UpcomingFragment extends Fragment {
    private BookingViewModel bookingViewModel;
    private CancelBookingViewModel cancelBookingViewModel;
    private BookingAdapter bookingAdapter;
    private TextView noBookingsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        noBookingsText = view.findViewById(R.id.no_bookings_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cancelBookingViewModel = new ViewModelProvider(requireActivity()).get(CancelBookingViewModel.class);
        bookingAdapter = new BookingAdapter(new ArrayList<>(), cancelBookingViewModel, false);
        recyclerView.setAdapter(bookingAdapter);

        bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        bookingViewModel.getUpcomingBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings.isEmpty()) {
                noBookingsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noBookingsText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                bookingAdapter.updateBookings(bookings);
            }
        });

        // Fetch user bookings
        bookingViewModel.fetchUserBookings();

        return view;
    }
}
