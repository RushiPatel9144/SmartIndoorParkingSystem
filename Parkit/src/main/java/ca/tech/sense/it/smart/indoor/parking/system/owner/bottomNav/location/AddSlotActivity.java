package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import static ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LauncherUtils.showToast;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSensor;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;

public class AddSlotActivity extends AppCompatActivity {

    private TextInputEditText slotIdEditText;
    private Spinner sensorTypeSpinner;
    private TextInputEditText batteryLevelEditText;
    private Button backButton;
    private Button addSlotButton;
    private String locationId ;
    private FirebaseAuth oAuth;
    private final ParkingUtility parkingUtility = new ParkingUtility();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_slot);

        oAuth = FirebaseAuthSingleton.getInstance();

        // Initialize views
        slotIdEditText = findViewById(R.id.slotId);
        sensorTypeSpinner = findViewById(R.id.sensorTypeSpinner);
        batteryLevelEditText = findViewById(R.id.batteryLevel);
        backButton = findViewById(R.id.backButton);
        addSlotButton = findViewById(R.id.addSlotButton);

        locationId = getIntent().getStringExtra("locationId");

        // Back button listener
        backButton.setOnClickListener(v -> finish());

        // Add slot button listener
        addSlotButton.setOnClickListener(v -> addSlot());
    }

    private void addSlot() {
        // Get input values
        String slotId = slotIdEditText.getText() != null ? slotIdEditText.getText().toString().trim() : "";
        String sensorType = sensorTypeSpinner.getSelectedItem().toString();
        String batteryLevelText = batteryLevelEditText.getText() != null ? batteryLevelEditText.getText().toString().trim() : "";

        // Validate inputs
        if (slotId.isEmpty()) {
            showToast(this, "Slot ID is required");
            return;
        }
        if (batteryLevelText.isEmpty()) {
            showToast(this, "Battery level is required");
            return;
        }

        float batteryLevel;
        try {
            batteryLevel = Float.parseFloat(batteryLevelText);
        } catch (NumberFormatException e) {
            showToast(this, "Invalid battery level");
            return;
        }

        if (batteryLevel < 0 || batteryLevel > 100) {
            showToast(this, "Battery level must be between 0 and 100");
            return;
        }

        // Create ParkingSensor object
        String sensorId = "sensor_" + System.currentTimeMillis(); // Unique sensor ID
        String lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        ParkingSensor sensor = new ParkingSensor(sensorId, lastUpdate, (int) batteryLevel, sensorType);
        ParkingSlot slot = new ParkingSlot(slotId, sensor);

        parkingUtility.addSlotToLocation(locationId, oAuth.getUid(), this, slot, sensor);
        finish();
    }

}
