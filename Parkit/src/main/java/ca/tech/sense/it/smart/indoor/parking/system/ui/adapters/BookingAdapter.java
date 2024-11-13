/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.annotation.SuppressLint;
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
    private CancelBookingViewModel cancelBookingViewModel;
    private boolean isHistory;

    public BookingAdapter(List<Booking> bookingList, CancelBookingViewModel cancelBookingViewModel, boolean isHistory) {
        this.bookingList = bookingList;
        this.cancelBookingViewModel = cancelBookingViewModel;
        this.isHistory = isHistory;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
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

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView bookingTitle;
        private TextView bookingAddress;
        private TextView bookingSlot;
        private TextView bookingTime;
        private TextView bookingPrice;
        private TextView bookingPassKey;
        private Button actionButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingTitle = itemView.findViewById(R.id.booking_title);
            bookingAddress = itemView.findViewById(R.id.booking_address);
            bookingSlot = itemView.findViewById(R.id.booking_slot);
            bookingTime = itemView.findViewById(R.id.booking_time);
            bookingPrice = itemView.findViewById(R.id.booking_price);
            bookingPassKey = itemView.findViewById(R.id.booking_pass_key);
            actionButton = itemView.findViewById(R.id.action_button);

            actionButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Booking booking = bookingList.get(position);
                    if (isHistory) {
                        // Clear booking history
                        cancelBookingViewModel.clearBookingHistory(booking.getId(), () -> {
                            bookingList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(itemView.getContext(), "Booking history cleared", Toast.LENGTH_SHORT).show();
                        }, error -> Toast.makeText(itemView.getContext(), "Failed to clear booking history: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        // Cancel booking
                        cancelBookingViewModel.cancelBooking(booking.getId(), () -> {
                            bookingList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(itemView.getContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                        }, error -> Toast.makeText(itemView.getContext(), "Failed to cancel booking: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }

        public void bind(Booking booking) {
            bookingTitle.setText(R.string.park_it);
            bookingAddress.setText(booking.getLocation());
            bookingSlot.setText(booking.getSlotNumber());
            bookingTime.setText(formatTime(booking.getStartTime(), booking.getEndTime()));
            bookingPrice.setText(String.format(Locale.getDefault(), "Price: $%.2f", booking.getPrice()));
            bookingPassKey.setText(booking.getPassKey());
            actionButton.setText(isHistory ? "Clear History" : "Cancel Booking");
        }

        private String formatTime(long startTime, long endTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime));
        }
    }
}
