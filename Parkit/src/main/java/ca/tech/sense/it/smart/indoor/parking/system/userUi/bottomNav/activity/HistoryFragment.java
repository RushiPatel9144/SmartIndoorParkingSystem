/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.BookingViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.adapters.BookingAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.CancelBookingViewModel;

public class HistoryFragment extends Fragment {
    private BookingViewModel bookingViewModel;
    private CancelBookingViewModel cancelBookingViewModel;
    private BookingAdapter bookingAdapter;
    private TextView noBookingsText;
    private TextView noBookingsText1;
    private ImageView noBookingsImage;
    private Button clearAllButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        noBookingsText = view.findViewById(R.id.no_bookings_text);
        noBookingsText1 = view.findViewById(R.id.no_bookings_text1);
        noBookingsImage = view.findViewById(R.id.parking_image);
        clearAllButton = view.findViewById(R.id.clear_all_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cancelBookingViewModel = new ViewModelProvider(requireActivity()).get(CancelBookingViewModel.class);
        bookingAdapter = new BookingAdapter(view, new ArrayList<>(), cancelBookingViewModel, bookingViewModel, R.layout.item_booking_history);
        recyclerView.setAdapter(bookingAdapter);

        bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        bookingViewModel.getHistoryBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings.isEmpty()) {
                noBookingsText.setVisibility(View.VISIBLE);
                noBookingsText1.setVisibility(View.VISIBLE);
                noBookingsImage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                clearAllButton.setVisibility(View.GONE);
            } else {
                noBookingsText.setVisibility(View.GONE);
                noBookingsText1.setVisibility(View.GONE);
                noBookingsImage.setVisibility(View.GONE);
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
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.clear_all_booking_history))
                .setMessage(R.string.are_you_sure_you_want_to_clear_all_booking_history)
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> cancelBookingViewModel.clearAllBookingHistory(() -> {
                    bookingAdapter.updateBookings(new ArrayList<>());
                    Toast.makeText(requireContext(), getString(R.string.all_booking_history_cleared), Toast.LENGTH_SHORT).show();
                }, error -> Toast.makeText(getContext(), getString(R.string.failed_to_clear_all_booking_history) + error.getMessage(), Toast.LENGTH_SHORT).show()))
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
