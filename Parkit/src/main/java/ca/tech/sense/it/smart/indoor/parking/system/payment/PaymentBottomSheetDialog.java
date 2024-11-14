package ca.tech.sense.it.smart.indoor.parking.system.payment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.booking.BookingBottomSheetDialog;
import ca.tech.sense.it.smart.indoor.parking.system.booking.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;

public class PaymentBottomSheetDialog extends BottomSheetDialog {

    private final Context context;
    private final Booking booking;
    private final BookingManager bookingManager;
    private final BookingBottomSheetDialog bookingBottomSheetDialog;

    public PaymentBottomSheetDialog(@NonNull Context context, Booking booking, BookingManager bookingManager, BookingBottomSheetDialog bookingBottomSheetDialog) {
        super(context);
        this.context = context;
        this.booking = booking;
        this.bookingManager = bookingManager;
        this.bookingBottomSheetDialog = bookingBottomSheetDialog;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_payment, null);
        setContentView(view);

        // Initialize UI elements
        TextView parkingNameTextView = view.findViewById(R.id.parkingNameTextView);
        TextView addressTextView = view.findViewById(R.id.addressTextView);
        TextView postalCodeTextView = view.findViewById(R.id.postalCodeTextView);
        TextView subtotalTextView = view.findViewById(R.id.subtotalTextView);
        TextView gstHstTextView = view.findViewById(R.id.gstHstTextView);
        TextView platformFeeTextView = view.findViewById(R.id.platformFeeTextView);
        TextView totalTextView = view.findViewById(R.id.totalTextView);
        Button applyPromoCodeButton = view.findViewById(R.id.applyPromoCodeButton);
        Button choosePaymentMethodButton = view.findViewById(R.id.choosePaymentMethodButton);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        // Set the booking details
        parkingNameTextView.setText(booking.getSlotNumber());
        addressTextView.setText(booking.getLocation());
        postalCodeTextView.setText(booking.getLocationId());

        // Calculate and set the total breakdown
        double subtotal = booking.getPrice();
        double gstHst = subtotal * 0.13; // Assuming 13% GST/HST
        double platformFee = (subtotal * 5)/100; // Assuming a fixed platform fee
        double total = subtotal + gstHst + platformFee;

        subtotalTextView.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        gstHstTextView.setText(String.format(Locale.getDefault(), "$%.2f", gstHst));
        platformFeeTextView.setText(String.format(Locale.getDefault(), "$%.2f", platformFee));
        totalTextView.setText(String.format(Locale.getDefault(), "$%.2f", total));

        // Set up the confirm button
        confirmButton.setOnClickListener(v -> {
            String selectedSlot = booking.getSlotNumber();
            String selectedTimeSlot = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(booking.getStartTime())) + " - " +
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(booking.getEndTime()));
            String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(booking.getStartTime()));

            if (selectedSlot != null && selectedTimeSlot != null && selectedDate != null) {
                bookingManager.confirmBooking(booking.getLocationId(), selectedSlot, selectedTimeSlot, selectedDate, booking.getLocation(), () -> {
                    Toast.makeText(context, R.string.booking_confirmed_and_saved, Toast.LENGTH_SHORT).show();
                    dismiss();
                }, error -> Toast.makeText(context, context.getString(R.string.failed_to_save_booking) + error.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, R.string.please_select_a_slot_date_and_time, Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the apply promo code button (functionality to be added later)
        applyPromoCodeButton.setOnClickListener(v -> {
            Toast.makeText(context, "Apply Promo Code functionality to be added", Toast.LENGTH_SHORT).show();
        });

        // Set up the choose payment method button (functionality to be added later)
        choosePaymentMethodButton.setOnClickListener(v -> {
            Toast.makeText(context, "Choose Payment Method functionality to be added", Toast.LENGTH_SHORT).show();
        });
    }
}
