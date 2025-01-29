package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

public class BookingConfirmationActivity extends AppCompatActivity {

    private String bookingId;
    private String NFC_TAG;
    private int progressStatus = 0;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private TextView nfcTagTextView;
    private ImageView backButton;
    private ProgressBar progressBar;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_confirmation);
        initializedUIComponents();
        setUpOnClickListeners();
        nfcTagTextView.setVisibility(View.GONE);
        // Start the progress animation
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1; // Increase progress
                handler.post(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(10); // Control speed (~3 sec total)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Hide ProgressBar after 3 seconds
            handler.post(() -> progressBar.setVisibility(View.GONE));
            handler.post(()-> nfcTagTextView.setVisibility(View.VISIBLE));
        }).start();


        Booking booking = (Booking) getIntent().getSerializableExtra("booking");
        if (booking != null) {
            bookingId = booking.getId();
            generateNFC();
        }
    }

    private void initializedUIComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        nfcTagTextView = findViewById(R.id.nfc_tag_tv);
        backButton = findViewById(R.id.nfc_screen_back_button);
        progressBar = findViewById(R.id.nfc_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }


    public void setUpOnClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }


    private String generateNFC() {

        NFC_TAG = UUID.randomUUID().toString();

        Log.d("NFC_TAG", "Generated NFC_TAG: " + NFC_TAG);

        DatabaseReference bookingRef = firebaseDatabase
                .getReference("users")
                .child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                .child("bookings")
                .child(bookingId); // Replace "bookingId" with the actual ID

        // Add the NFC field to the existing booking
        Map<String, Object> updates = new HashMap<>();
        updates.put("nfcTag", NFC_TAG);

        bookingRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated the booking
                    Toast.makeText(this, "NFC Tag added successfully!", Toast.LENGTH_SHORT).show();
                    nfcTagTextView.setText(NFC_TAG);
                    // Trigger NFC emulation
                    emulateNFCTag();
                })
                .addOnFailureListener(e -> {
                    // Failed to update the booking
                    Toast.makeText(this, "Failed to add NFC Tag: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        return NFC_TAG;
    }



    private void emulateNFCTag() {
        // Start the NFC emulator service
        if (NFC_TAG != null) {
            Log.d("NFC_TAG", "Emulating NFC tag: " + NFC_TAG);

            // Start the NFC emulation service (HostApduService)
            Intent serviceIntent = new Intent(this, NfcEmulatorService.class);
            Booking booking = (Booking) getIntent().getSerializableExtra("booking");
            if (booking != null) {
                serviceIntent.putExtra("bookingId", booking.getId());
            }
            startService(serviceIntent);
        } else {
            Log.d("NFC_TAG", "Error: No NFC tag generated!");
        }
    }


}