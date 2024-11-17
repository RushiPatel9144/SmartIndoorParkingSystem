package ca.tech.sense.it.smart.indoor.parking.system.payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.booking.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.stripe.PaymentActivity;

public class PaymentBottomSheetFragment extends BottomSheetDialogFragment {

    private final Booking booking;
    private final BookingManager bookingManager;

    private TextView parkingNameTextView;
    private TextView addressTextView;
    private TextView postalCodeTextView;
    private TextView subtotalTextView;
    private TextView gstHstTextView;
    private TextView platformFeeTextView;
    private TextView totalTextView;
    private Button applyPromoCodeButton;
    private Button choosePaymentMethodButton;
    private Button confirmButton;

    public PaymentBottomSheetFragment( Booking booking, BookingManager bookingManager) {
        this.booking = booking;
        this.bookingManager = bookingManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.dialog_payment, container, false);

        initializeUIElements(view);
        setBookingDetails();
        calculateTotalBreakdown();
        setButtonListeners();

        return view;
    }


    private void initializeUIElements(View view) {
        parkingNameTextView = view.findViewById(R.id.parkingNameTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        postalCodeTextView = view.findViewById(R.id.postalCodeTextView);
        subtotalTextView = view.findViewById(R.id.subtotalTextView);
        gstHstTextView = view.findViewById(R.id.gstHstTextView);
        platformFeeTextView = view.findViewById(R.id.platformFeeTextView);
        totalTextView = view.findViewById(R.id.totalTextView);
        applyPromoCodeButton = view.findViewById(R.id.applyPromoCodeButton);
        confirmButton = view.findViewById(R.id.confirmButton);
    }

    private void setBookingDetails() {
        parkingNameTextView.setText(booking.getSlotNumber());
        addressTextView.setText(booking.getLocation());
        postalCodeTextView.setText(booking.getLocationId());
    }

    private void calculateTotalBreakdown() {
        double subtotal = booking.getPrice();
        double gstHst = subtotal * 0.13; // Assuming 13% GST/HST
        double platformFee = (subtotal * 5) / 100; // Assuming a fixed platform fee of 5%
        double total = subtotal + gstHst + platformFee;

        subtotalTextView.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        gstHstTextView.setText(String.format(Locale.getDefault(), "$%.2f", gstHst));
        platformFeeTextView.setText(String.format(Locale.getDefault(), "$%.2f", platformFee));
        totalTextView.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    private void setButtonListeners() {
        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PaymentActivity.class);
            startActivity(intent);
        });
        applyPromoCodeButton.setOnClickListener(v -> showToast("Apply Promo Code functionality to be added"));
    }

    private void confirmBooking() {
        String selectedSlot = booking.getSlotNumber();
        String selectedTimeSlot = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(booking.getStartTime())) + " - " +
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(booking.getEndTime()));
        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(booking.getStartTime()));

        if (selectedSlot != null && selectedTimeSlot != null && selectedDate != null) {
            bookingManager.confirmBooking(booking.getLocationId(), selectedSlot, selectedTimeSlot, selectedDate, booking.getLocation(),
                    () -> {
                        showToast(R.string.booking_confirmed_and_saved);
                        dismiss();
                    },
                    error -> showToast(requireContext().getString(R.string.failed_to_save_booking) + error.getMessage())
            );
        } else {
            showToast(R.string.please_select_a_slot_date_and_time);
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int resId) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
    }

    // Method to show the BottomSheetDialogFragment from Activity or Fragment
    public void showPaymentBottomSheet(FragmentManager fragmentManager) {
        this.show(fragmentManager, "PaymentBottomSheetFragment");
    }
}
