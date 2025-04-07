package ca.tech.sense.it.smart.indoor.parking.system.gate;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;

public class OpenGateActivity extends AppCompatActivity {

    private TextView statusTextView,tempratureTextView,airQualityTextView,carDetectedTextView;
    private Button gateButton;
    private DatabaseReference gateRef;
    private String bookingId;
    private ImageView backButton;
    private final String TAG = "OpenGateActivity";
    private int countdown = 5; // Countdown timer (5 seconds)
    private Handler countdownHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_open_gate_activity);

        // Handle window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        initializeUI();

        // Retrieve booking ID from Intent
        bookingId = retrieveIntentData();

        if (bookingId == null) {
            Toast.makeText(this, "No Booking ID Found!", Toast.LENGTH_SHORT).show();
            finish(); // Exit if no booking ID found
            return;
        }

        // Fetch location ID from Firebase based on booking ID
        fetchLocationId(bookingId);
        fetchSensorData();
    }

    private void fetchSensorData() {
        DatabaseReference sensorDataRef = FirebaseDatabase.getInstance().getReference();

// Fetching air quality data
        // Fetching air quality index data continuously
        sensorDataRef.child("AirQuality").child("iaq_index").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double iaqIndex = snapshot.getValue(Double.class);
                    if (iaqIndex != null) {
                        // Round the value to the nearest integer
                        int roundedIaqIndex = (int) Math.round(iaqIndex);
                        airQualityTextView.setText("Air Quality Index: " + String.valueOf(roundedIaqIndex)); // Set the rounded value
                    } else {
                        airQualityTextView.setText("Air Quality Index: No data");
                    }
                } else {
                    airQualityTextView.setText("Air Quality Index: No data");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("AirQuality", "Error fetching air quality data: " + error.getMessage());
            }
        });

// Fetching car detected data continuously
        sensorDataRef.child("Parking").child("car_detected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean carDetected = snapshot.getValue(Boolean.class);
                    if (carDetected != null) {
                        carDetectedTextView.setText(carDetected ? "Car Detected: Yes" : "Car Detected: No");
                    } else {
                        carDetectedTextView.setText("Car Detected: No data");
                    }
                } else {
                    carDetectedTextView.setText("Car Detected: No data");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Parking", "Error fetching car detected data: " + error.getMessage());
            }
        });

    }

    private void initializeUI() {
        statusTextView = findViewById(R.id.txtStatus);
        tempratureTextView = findViewById(R.id.temperature);
        airQualityTextView = findViewById(R.id.airQuality);
        carDetectedTextView = findViewById(R.id.carDetected);
        backButton = findViewById(R.id.btn_back);
        gateButton = findViewById(R.id.btnOpenGate);
        gateButton.setEnabled(false); // Disable button until location ID is fetched
        backButton.setOnClickListener(v -> {finish();});
    }

    private String retrieveIntentData() {
        return getIntent().getStringExtra("bookingId");
    }

    private void fetchLocationId(String bookingId) {
        DatabaseReference locationIdRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuthSingleton.getInstance().getCurrentUser().getUid())
                .child("bookings")
                .child(bookingId)
                .child("locationId");

        locationIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String locationId = snapshot.getValue(String.class);
                    Log.d(TAG, "Fetched locationId: " + locationId);
                    setupGateReference(locationId);
                } else {
                    Toast.makeText(OpenGateActivity.this, "Location ID not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to fetch locationId", error.toException());
            }
        });
    }

    private void setupGateReference(String locationId) {
        gateRef = FirebaseDatabase.getInstance()
                .getReference("parkingLocations")
                .child(locationId)
                .child("open_gate");

        gateButton.setEnabled(true); // Enable the button once ready
        gateButton.setOnClickListener(v -> openGate());
    }

    private void openGate() {
        if (gateRef == null) {
            Toast.makeText(this, "Gate reference is not ready!", Toast.LENGTH_SHORT).show();
            return;
        }

        statusTextView.setText("Gate Opening...");
        gateButton.setEnabled(false);

        // Open the gate in Firebase
        gateRef.setValue(true).addOnSuccessListener(unused -> {
            // Successfully opened the gate, now start countdown and close the gate after 5 seconds
            new Handler().postDelayed(() -> {
                gateRef.setValue(false); // Close Gate after 5 sec

                statusTextView.setText("Gate Closing...");
            }, 5000); // 5 seconds delay before closing

            new Handler().postDelayed(() -> {
                statusTextView.setText("Gate Closed");
                gateButton.setEnabled(true); // Re-enable button
            }, 8000); // 5 seconds delay before closing
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to open gate", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to set open_gate to true", e);
        });
    }
}
