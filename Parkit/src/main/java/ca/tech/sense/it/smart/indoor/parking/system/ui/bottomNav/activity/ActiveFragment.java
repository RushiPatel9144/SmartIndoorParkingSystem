/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.BookingViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.BookingAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.CancelBookingViewModel;

public class ActiveFragment extends BaseNetworkFragment {
    private BookingViewModel bookingViewModel;
    private CancelBookingViewModel cancelBookingViewModel;
    private BookingAdapter bookingAdapter;
    private TextView noBookingsText;
    private TextView noBookingsText2;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        noBookingsText = view.findViewById(R.id.no_bookings_text);
        noBookingsText2= view.findViewById(R.id.park_now);
        ImageView parkingImage = view.findViewById(R.id.parking_image); // Add this lines


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cancelBookingViewModel = new ViewModelProvider(requireActivity()).get(CancelBookingViewModel.class);
        bookingAdapter = new BookingAdapter(view, new ArrayList<>(), cancelBookingViewModel, bookingViewModel, R.layout.item_booking_active);
        recyclerView.setAdapter(bookingAdapter);

        bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        bookingViewModel.getActiveBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings.isEmpty()) {
                noBookingsText2.setVisibility(View.VISIBLE);
                noBookingsText.setVisibility(View.VISIBLE);
                parkingImage.setVisibility(View.VISIBLE); // Add this line
                recyclerView.setVisibility(View.GONE);
            } else {
                noBookingsText2.setVisibility(View.GONE);
                noBookingsText.setVisibility(View.GONE);
                parkingImage.setVisibility(View.GONE); // Add this line
                recyclerView.setVisibility(View.VISIBLE);
                bookingAdapter.updateBookings(bookings);
            }
        });

        // Fetch user bookings
        bookingViewModel.fetchUserBookings();

        return view;
    }
}
