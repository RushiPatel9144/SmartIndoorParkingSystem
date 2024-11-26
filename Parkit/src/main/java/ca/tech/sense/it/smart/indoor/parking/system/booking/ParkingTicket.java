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

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ParkingTicket extends AppCompatActivity {

    private TextView addressTitle;
    private TextView addressText;
    private ProgressBar progressBar;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket);

        TextView referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button getDirectionButton = findViewById(R.id.getDirectionButton);
        addressTitle = findViewById(R.id.addressTitle);
        addressText = findViewById(R.id.addressText);
        progressBar = findViewById(R.id.progressBar);

        // Get data from the intent
        Intent intent = getIntent();
        if (intent != null) {
            try {
                String passkey = intent.getStringExtra("passkey");
                address = intent.getStringExtra("address");

                // Check if passkey is not null and address is provided
                if (!TextUtils.isEmpty(passkey)) {
                    referenceNumberTextView.setText(passkey);
                    progressBar.setVisibility(View.VISIBLE);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!TextUtils.isEmpty(address)) {
                            updateUIWithBookingDetails(address);
                        } else {
                            showToast("Address not provided");
                        }
                        progressBar.setVisibility(View.GONE);
                    }, 2000);
                } else {
                    showToast("Passkey is missing");
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

    private void updateUIWithBookingDetails(String address) {
        addressTitle.setText("Parking Address");
        addressText.setText(address);

        if (TextUtils.isEmpty(address)) {
            showToast("Failed to fetch location details");
        }
    }

    private void openMap() {
        try {
            if (address != null) {
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
