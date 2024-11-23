package ca.tech.sense.it.smart.indoor.parking.system.booking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ParkingTicket extends AppCompatActivity {

    private TextView referenceNumberTextView;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket); // Update to your layout

        referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        cancelButton = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("passkey")) {
            String passkey = intent.getStringExtra("passkey");
            if (passkey != null) {
                referenceNumberTextView.setText(passkey);
            } else {
                showToast("Passkey is missing");
            }
        } else {
            showToast("Intent data is missing");
        }

        cancelButton.setOnClickListener(v -> finish());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}


