package ca.tech.sense.it.smart.indoor.parking.system.booking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ParkingTicket extends AppCompatActivity {

    private TextView referenceNumberTextView;
    private TextView addressTitle;
    private TextView addressText;
    private Button cancelButton;
    private Button getDirectionButton;
    private String address; // Variable to store the address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket);

        referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        cancelButton = findViewById(R.id.cancelButton);
        getDirectionButton = findViewById(R.id.getDirectionButton);
        addressTitle = findViewById(R.id.addressTitle);
        addressText = findViewById(R.id.addressText);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("passkey")) {
            String passkey = intent.getStringExtra("passkey");
            if (!TextUtils.isEmpty(passkey)) {
                referenceNumberTextView.setText(passkey);
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
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                            updateUIWithBookingDetails(booking);
                        }
                    }
                } else {
                    showToast("Booking details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to fetch booking details: " + error.getMessage());
            }
        });
    }

    private void updateUIWithBookingDetails(Booking booking) {
        String addressName = booking.getTitle();
        address = booking.getLocation(); // Store the address

        addressTitle.setText(addressName);
        addressText.setText(String.format("%s", address));
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
