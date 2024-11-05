package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;

import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
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

    public void updateBookings(List<Booking> bookings) {
        this.bookingList = bookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {

        private TextView bookingTitle;
        private TextView bookingTime;
        private TextView bookingLocation;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingTitle = itemView.findViewById(R.id.booking_title);
            bookingTime = itemView.findViewById(R.id.booking_time);
            bookingLocation = itemView.findViewById(R.id.booking_location);
        }

        public void bind(Booking booking) {
            bookingTitle.setText(booking.getTitle());
            bookingTime.setText(formatTime(booking.getStartTime(), booking.getEndTime()));
            bookingLocation.setText(booking.getLocation());
        }

        private String formatTime(long startTime, long endTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(startTime)) + " - " + sdf.format(new Date(endTime));
        }
    }
}

