package ca.tech.sense.it.smart.indoor.parking.system.booking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Objects;

public class ParkingTicket extends AppCompatActivity {

    private TextView addressTitle;
    private TextView addressText;
    private ProgressBar progressBar; // ProgressBar variable
    private String address; // Variable to store the address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket);

        TextView referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button getDirectionButton = findViewById(R.id.getDirectionButton);
        addressTitle = findViewById(R.id.addressTitle);
        addressText = findViewById(R.id.addressText);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("passkey")) {
            String passkey = intent.getStringExtra("passkey");
            if (!TextUtils.isEmpty(passkey)) {
                referenceNumberTextView.setText(passkey);
                progressBar.setVisibility(View.VISIBLE); // Show ProgressBar
                fetchBookingDetails(passkey); // Fetch booking details
            } else {
                showToast("Passkey is missing");
            }
        } else {
            showToast("Intent data is missing");
        }

        cancelButton.setOnClickListener(v -> finish());
        getDirectionButton.setOnClickListener(v -> openMap());
    }

    private void fetchBookingDetails(String passkey) {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference("users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("bookings")
                .orderByChild("passKey")
                .equalTo(passkey).getRef();

        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        Booking booking = bookingSnapshot.getValue(Booking.class);
                        if (booking != null) {
                            // Debug log
                            Log.d("ParkingTicket", "Fetched booking: " + booking.getLocation());
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                updateUIWithBookingDetails(booking);
                                progressBar.setVisibility(View.GONE); // Hide ProgressBar
                            }, 2000); // 2 seconds delay
                        }
                    }
                } else {
                    showToast("Booking details not found");
                    progressBar.setVisibility(View.GONE); // Hide ProgressBar
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to fetch booking details: " + error.getMessage());
                progressBar.setVisibility(View.GONE); // Hide ProgressBar
            }
        });
    }

    private void updateUIWithBookingDetails(Booking booking) {
        String addressName = booking.getTitle();
        address = booking.getLocation(); // Store the address

        addressTitle.setText(addressName);
        addressText.setText(String.format("%s", address));

        // Show toast if location is not fetched
        if (TextUtils.isEmpty(address)) {
            showToast("Failed to fetch location details");
        }
    }

    private void openMap() {
        // Use the stored address for the destination
        String destination = "geo:0,0?q=" + Uri.encode(address);
        Uri gmmIntentUri = Uri.parse(destination);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            showToast("Google Maps is not installed");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
