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
    private String address;  // This should hold the address value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket);

        TextView referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button getDirectionButton = findViewById(R.id.getDirectionButton);
        addressTitle = findViewById(R.id.addressTitle);
        addressText = findViewById(R.id.addressText);
        parkingTimeTitle = findViewById(R.id.parkingTimeTitle);
        parkingTimeText = findViewById(R.id.parkingTimeText);
        priceTitle = findViewById(R.id.priceTitle);
        priceText = findViewById(R.id.priceText);
        progressBar = findViewById(R.id.progressBar);

        // Get data from the intent
        Intent intent = getIntent();
        if (intent != null) {
            try {
                // Retrieve the Booking object from the intent
                Booking booking = (Booking) intent.getSerializableExtra("booking");

                if (booking != null) {
                    String passkey = booking.getPassKey(); // Get the passkey from the Booking object
                    address = booking.getLocation(); // Assign the address to the class-level variable
                    long startTime = booking.getStartTime(); // Get the start time
                    long endTime = booking.getEndTime(); // Get the end time
                    double price = booking.getPrice(); // Get the price

                    // Check if passkey is not null
                    if (!TextUtils.isEmpty(passkey)) {
                        referenceNumberTextView.setText(passkey);
                        progressBar.setVisibility(View.VISIBLE);

                        // Format parking time and price
                        String parkingTime = formatParkingTime(startTime, endTime);
                        String formattedPrice = String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), price);

                        // Simulate some processing or delay
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (!TextUtils.isEmpty(address)) {
                                updateUIWithBookingDetails(address, parkingTime, formattedPrice); // Update UI with address, time, and price
                            } else {
                                showToast("Address not provided");
                            }
                            progressBar.setVisibility(View.GONE);
                        }, 2000);
                    } else {
                        showToast("Passkey is missing in booking details");
                    }
                } else {
                    showToast("Booking details are missing in intent");
                }
            } catch (Exception e) {
                showToast("An error occurred: " + e.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        } else {
            showToast("Intent data is missing");
        }

        cancelButton.setOnClickListener(v -> finish());
        getDirectionButton.setOnClickListener(v -> openMap());
    }

    private void updateUIWithBookingDetails(String address, String parkingTime, String price) {
        addressTitle.setText("Parking Address");
        addressText.setText(address);
        parkingTimeTitle.setText("Parking Time");
        parkingTimeText.setText(parkingTime);
        priceTitle.setText("Price");
        priceText.setText(price);

        if (TextUtils.isEmpty(address)) {
            showToast("Failed to fetch location details");
        }
    }

    private String formatParkingTime(long startTime, long endTime) {
        // You can use SimpleDateFormat to format the time
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String start = sdf.format(new Date(startTime));
        String end = sdf.format(new Date(endTime));
        return start + " - " + end;
    }

    private void openMap() {
        try {
            if (address != null && !address.isEmpty()) {  // Ensure the address is not null or empty
                String destination = "geo:0,0?q=" + Uri.encode(address);
                Uri gmmIntentUri = Uri.parse(destination);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    showToast("Google Maps is not installed");
                }
            } else {
                showToast("Address is missing for directions");
            }
        } catch (Exception e) {
            showToast("An error occurred while opening the map: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
