package ca.tech.sense.it.smart.indoor.parking.system.booking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ParkingTicket extends AppCompatActivity {

    private TextView referenceNumberTextView;
    private Button cancelButton;
    private Button getDirectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ticket); // Update to your layout

        referenceNumberTextView = findViewById(R.id.referenceNumberTextView);
        cancelButton = findViewById(R.id.cancelButton);
        getDirectionButton = findViewById(R.id.getDirectionButton);

        if (referenceNumberTextView == null || cancelButton == null || getDirectionButton == null) {
            showToast("UI elements not found");
            return;
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("passkey")) {
            String passkey = intent.getStringExtra("passkey");
            if (!TextUtils.isEmpty(passkey)) {
                referenceNumberTextView.setText(passkey);
            } else {
                showToast("Passkey is missing");
            }
        } else {
            showToast("Intent data is missing");
        }

        cancelButton.setOnClickListener(v -> finish());

        getDirectionButton.setOnClickListener(v -> openMap());
    }

    private void openMap() {
        // Replace with your destination coordinates
        String destination = "geo:0,0?q=Building+No.+26,+Al+Barsha+2,+Street+43+A.T.,+Dubai,+UAE";
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
