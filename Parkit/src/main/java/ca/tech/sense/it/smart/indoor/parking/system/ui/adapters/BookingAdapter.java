/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.CancelBookingViewModel;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private final CancelBookingViewModel cancelBookingViewModel;
    private final int layoutResourceId;

    public BookingAdapter(List<Booking> bookingList, CancelBookingViewModel cancelBookingViewModel, int layoutResourceId) {
        this.bookingList = bookingList;
        this.cancelBookingViewModel = cancelBookingViewModel;
        this.layoutResourceId = layoutResourceId;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateBookings(List<Booking> bookings) {
        this.bookingList = bookings;
        notifyDataSetChanged();
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookingTitle;
        private final TextView bookingAddress;
        private final TextView bookingSlot;
        private final TextView bookingTime;
        private final TextView bookingPrice;
        private final TextView bookingPassKey;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingTitle = itemView.findViewById(R.id.booking_title);
            bookingAddress = itemView.findViewById(R.id.booking_address);
            bookingSlot = itemView.findViewById(R.id.booking_slot);
            bookingTime = itemView.findViewById(R.id.booking_time);
            bookingPrice = itemView.findViewById(R.id.booking_price);
            bookingPassKey = itemView.findViewById(R.id.booking_pass_key);
            Button cancelButton = itemView.findViewById(R.id.cancel_button);

            if (cancelButton != null) {
                cancelButton.setOnClickListener(v -> {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Booking booking = bookingList.get(position);
                        showCancelConfirmationDialog(itemView.getContext(), booking);
                    }
                });
            }
        }

        private void showCancelConfirmationDialog(Context context, Booking booking) {
            new AlertDialog.Builder(context)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel this booking?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < bookingList.size()) {
                            cancelBookingViewModel.cancelBooking(booking.getId(), () -> {
                                if (position < bookingList.size()) {
                                    bookingList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show();
                                }
                            }, error -> Toast.makeText(context, "Failed to cancel booking: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        public void bind(Booking booking) {
            bookingTitle.setText(R.string.park_it);
            bookingAddress.setText(booking.getLocation());
            bookingSlot.setText(booking.getSlotNumber());
            bookingTime.setText(formatTime(booking.getStartTime(), booking.getEndTime()));
            bookingPrice.setText(String.format(Locale.getDefault(), "Price: $%.2f", booking.getPrice()));
            bookingPassKey.setText(booking.getPassKey());
        }

        private String formatTime(long startTime, long endTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime));
        }
    }

}