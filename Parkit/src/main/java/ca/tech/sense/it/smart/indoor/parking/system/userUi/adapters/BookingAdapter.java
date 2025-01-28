/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.userUi.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import ca.tech.sense.it.smart.indoor.parking.system.booking.NfcEmulatorService;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.BookingViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.CancelBookingViewModel;

import androidx.recyclerview.widget.DiffUtil;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private View view;
    private final CancelBookingViewModel cancelBookingViewModel;
    private final BookingViewModel bookingViewModel;
    private final int layoutResourceId;

    public BookingAdapter(View view, List<Booking> bookingList, CancelBookingViewModel cancelBookingViewModel, BookingViewModel bookingViewModel, int layoutResourceId) {
        this.bookingList = bookingList;
        this.cancelBookingViewModel = cancelBookingViewModel;
        this.bookingViewModel = bookingViewModel;
        this.layoutResourceId = layoutResourceId;
        this.view = view;
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

    public void updateBookings(List<Booking> newBookings) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BookingDiffCallback(bookingList, newBookings));
        bookingList.clear();
        bookingList.addAll(newBookings);
        diffResult.dispatchUpdatesTo(this);
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookingAddress;
        private final TextView bookingSlot;
        private final TextView bookingTime;
        private final TextView bookingPrice;
        private final TextView bookingPassKey;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingAddress = itemView.findViewById(R.id.booking_address);
            bookingSlot = itemView.findViewById(R.id.booking_slot);
            bookingTime = itemView.findViewById(R.id.booking_time);
            bookingPrice = itemView.findViewById(R.id.booking_price);
            bookingPassKey = itemView.findViewById(R.id.booking_pass_key);
            Button cancelButton = itemView.findViewById(R.id.cancel_button);
            Button NfcButton = itemView.findViewById(R.id.NFC_button_upcomingBooking);

            if (cancelButton != null) {
                cancelButton.setOnClickListener(v -> {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Booking booking = bookingList.get(position);
                        showCancelConfirmationDialog(itemView.getContext(), booking);
                    }
                });
            }

            //nfc button pressed
            NfcButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Booking booking = bookingList.get(position);
                    // Start the NFC emulation service (HostApduService)

                }
            });

        }

        private void showCancelConfirmationDialog(Context context, Booking booking) {
            DialogUtil.showMessageDialog(context, context.getString(R.string.cancel_booking),
                    context.getString(R.string.are_you_sure_you_want_to_cancel_this_booking_please_note_that_you_may_not_receive_a_full_refund) +
                            context.getString(R.string.and_the_refund_will_be_processed_within_7_days_or_more_credited_to_the_bank_account_used_for_the_payment),
                    context.getString(R.string.confirm), new DialogUtil.DialogCallback() {
                        @Override
                        public void onConfirm() {
                            int position = getBindingAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && position < bookingList.size()) {
                                cancelBookingViewModel.cancelBooking(booking, () -> {
                                    bookingList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, bookingList.size());
                                    if (view!= null) {
                                        Snackbar.make(view, R.string.booking_cancelled_and_refunded_the_money_may_take_a_few_days_to_reach_your_bank_account, BaseTransientBottomBar.LENGTH_LONG).show();
                                    }
                                    // Refresh the list
                                    bookingViewModel.fetchUserBookings();
                                }, error -> Toast.makeText(context, context.getString(R.string.failed_to_cancel_booking) + error.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                        @Override
                        public void onCancel() {
                            // nothing
                        }
                    });
        }

        public void bind(Booking booking) {
            bookingAddress.setText(booking.getLocation());
            bookingSlot.setText(booking.getSlotNumber());
            bookingTime.setText(formatTime(booking.getStartTime(), booking.getEndTime()));
            bookingPrice.setText(String.format(Locale.getDefault(), "Price: " + " $ %.2f", booking.getTotalPrice()));
            bookingPassKey.setText(booking.getPassKey());
        }

        private String formatTime(long startTime, long endTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime));
        }
    }

    private static class BookingDiffCallback extends DiffUtil.Callback {
        private final List<Booking> oldList;
        private final List<Booking> newList;

        public BookingDiffCallback(List<Booking> oldList, List<Booking> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Booking oldBooking = oldList.get(oldItemPosition);
            Booking newBooking = newList.get(newItemPosition);
            return oldBooking.equals(newBooking);
        }
    }
}
