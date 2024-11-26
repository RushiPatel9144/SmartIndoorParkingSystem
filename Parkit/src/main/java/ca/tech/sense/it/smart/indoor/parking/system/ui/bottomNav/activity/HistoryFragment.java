/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.BookingViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.BookingAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.CancelBookingViewModel;

public class HistoryFragment extends Fragment {
    private BookingViewModel bookingViewModel;
    private CancelBookingViewModel cancelBookingViewModel;
    private BookingAdapter bookingAdapter;
    private TextView noBookingsText;
    private Button clearAllButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        noBookingsText = view.findViewById(R.id.no_bookings_text);
        clearAllButton = view.findViewById(R.id.clear_all_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cancelBookingViewModel = new ViewModelProvider(requireActivity()).get(CancelBookingViewModel.class);
        bookingAdapter = new BookingAdapter(new ArrayList<>(), cancelBookingViewModel, bookingViewModel, R.layout.item_booking_history);
        recyclerView.setAdapter(bookingAdapter);

        bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        bookingViewModel.getHistoryBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings.isEmpty()) {
                noBookingsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                clearAllButton.setVisibility(View.GONE);
            } else {
                noBookingsText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                clearAllButton.setVisibility(View.VISIBLE);
                bookingAdapter.updateBookings(bookings);
            }
        });

        // Fetch user bookings
        bookingViewModel.fetchUserBookings();

        // Set up the clear all button
        clearAllButton.setOnClickListener(v -> showClearAllConfirmationDialog());

        return view;
    }

    private void showClearAllConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear All Booking History")
                .setMessage("Are you sure you want to clear all booking history?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    cancelBookingViewModel.clearAllBookingHistory(() -> {
                        bookingAdapter.updateBookings(new ArrayList<>());
                        Toast.makeText(getContext(), "All booking history cleared", Toast.LENGTH_SHORT).show();
                    }, error -> Toast.makeText(getContext(), "Failed to clear all booking history: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}





