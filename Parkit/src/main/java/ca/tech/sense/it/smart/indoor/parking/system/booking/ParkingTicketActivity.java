package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;

public class ParkingTicketActivity extends AppCompatActivity {

    private ImageView qrCodeImage;
    private TextView parkingTicketNumber, parkingDetails, parkingAddress;
    private Button getDirectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket);

        // Initialize UI elements
        qrCodeImage = findViewById(R.id.qrCodeImage);
        parkingTicketNumber = findViewById(R.id.parkingTicketNumber);
        parkingDetails = findViewById(R.id.parkingDetails);
        parkingAddress = findViewById(R.id.parkingAddress);
        getDirectionButton = findViewById(R.id.getDirectionButton);

        // Get Booking Data from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("booking")) {
            Booking booking = (Booking) intent.getSerializableExtra("booking");
        }
    }
}

