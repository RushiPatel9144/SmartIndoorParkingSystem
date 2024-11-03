package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Date;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.BookingManager;

public class BookingDetailsFragment extends Fragment {

    private EditText etName, etTiming, etPaymentMethod;
    private Button btnProceedToPayment;
    private BookingManager bookingManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_details, container, false);

        bookingManager = BookingManager.getInstance(); // Use getInstance() to initialize BookingManager
        etName = view.findViewById(R.id.et_name);
        etTiming = view.findViewById(R.id.et_timing);
        btnProceedToPayment = view.findViewById(R.id.btn_proceed_to_payment);


        btnProceedToPayment.setOnClickListener(v -> {
            // Assuming you have a way to get the date, here just creating a new Date
            Date selectedDate = new Date(); // Replace this with your actual date selection logic

            String name = etName.getText().toString().trim();
            String timing = etTiming.getText().toString().trim();

            if (!name.isEmpty() && !timing.isEmpty()) {
                proceedToPayment(name, timing, selectedDate);
            } else {
                Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void proceedToPayment(String name, String timing, Date date) {
        // Your payment processing logic
        // Example: Save booking details using bookingManager
        bookingManager.saveBooking(name, timing, date);
        Toast.makeText(getContext(), "Booking confirmed!", Toast.LENGTH_SHORT).show();
    }

}
