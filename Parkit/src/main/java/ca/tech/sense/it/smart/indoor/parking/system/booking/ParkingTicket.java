package ca.tech.sense.it.smart.indoor.parking.system.booking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

public class ParkingTicket extends AppCompatActivity {

    private TextView addressTitle;
    private TextView addressText;
    private TextView parkingTimeTitle;
    private TextView parkingTimeText;
    private TextView priceTitle;
    private TextView priceText;
    private ProgressBar progressBar;
    private String address;  // Holds the address value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket);

        initializeUIComponents();

        // Get data from the intent
        Intent intent = getIntent();
        if (intent != null) {
            handleBookingData(intent);
        } else {
            showToast(R.string.error_intent_missing);
        }

        // Set up button click listeners
        setUpButtonListeners();
    }

    private void initializeUIComponents() {
        addressTitle = findViewById(R.id.addressTitle);
        addressText = findViewById(R.id.addressText);
        parkingTimeTitle = findViewById(R.id.parkingTimeTitle);
        parkingTimeText = findViewById(R.id.parkingTimeText);
        priceTitle = findViewById(R.id.priceTitle);
        priceText = findViewById(R.id.priceText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void handleBookingData(Intent intent) {
        try {
            Booking booking;
            booking = (Booking) intent.getSerializableExtra("booking");

            if (booking != null) {
                processBookingData(booking);
            } else {
                showToast(R.string.error_booking_missing);
            }
        } catch (ClassCastException e) {
            showToast(R.string.error_invalid_booking_data);
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            showToast(getString(R.string.error_generic, e.getMessage()));
            progressBar.setVisibility(View.GONE);
        }
    }

    private void processBookingData(Booking booking) {
        String passkey = booking.getPassKey();
        address = booking.getLocation();
        long startTime = booking.getStartTime();
        long endTime = booking.getEndTime();
        double price = booking.getTotalPrice();

        if (!TextUtils.isEmpty(passkey)) {
            displayBookingDetails(booking, startTime, endTime, price);
        } else {
            showToast(R.string.error_passkey_missing);
        }
    }

    private void displayBookingDetails(Booking booking, long startTime, long endTime, double price) {
        TextView referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        referenceNumberTextView.setText(booking.getPassKey());
        progressBar.setVisibility(View.VISIBLE);

        String parkingTime = formatParkingTime(startTime, endTime);
        String formattedPrice = String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), price);

        // Simulate processing delay (e.g., network call)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!TextUtils.isEmpty(address)) {
                updateUIWithBookingDetails(address, parkingTime, formattedPrice);
            } else {
                showToast(R.string.error_address_missing);
            }
            progressBar.setVisibility(View.GONE);
        }, 2000);
    }

    private void updateUIWithBookingDetails(String address, String parkingTime, String price) {
        addressTitle.setText(getString(R.string.parking_address));
        addressText.setText(address);
        parkingTimeTitle.setText(getString(R.string.parking_time));
        parkingTimeText.setText(parkingTime);
        priceTitle.setText(getString(R.string.price));
        priceText.setText(price);
    }

    private String formatParkingTime(long startTime, long endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String start = sdf.format(new Date(startTime));
        String end = sdf.format(new Date(endTime));
        return start + " - " + end;
    }

    private void setUpButtonListeners() {
        Button cancelButton = findViewById(R.id.cancelButton);
        Button getDirectionButton = findViewById(R.id.getDirectionButton);

        cancelButton.setOnClickListener(v -> finish());
        getDirectionButton.setOnClickListener(v -> openMap());
    }

    private void openMap() {
        try {
            if (address != null && !address.isEmpty()) {
                String destination = "geo:0,0?q=" + Uri.encode(address);
                Uri gmmIntentUri = Uri.parse(destination);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    showToast(R.string.error_google_maps_not_installed);
                }
            } else {
                showToast(R.string.error_address_missing_for_directions);
            }
        } catch (Exception e) {
            showToast(getString(R.string.error_generic, e.getMessage()));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }
}
