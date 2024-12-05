package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleSlot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;
import java.util.Objects;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager.ParkingSlotManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSensor;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class AddSlotActivity extends AppCompatActivity {

    private TextInputEditText slotIdEditText;
    private Spinner sensorTypeSpinner;
    private TextInputEditText batteryLevelEditText;
    private String locationId;
    private final ParkingSlotManager parkingSlotManager = new ParkingSlotManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_slot);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        slotIdEditText = findViewById(R.id.slotId);
        sensorTypeSpinner = findViewById(R.id.sensorTypeSpinner);
        batteryLevelEditText = findViewById(R.id.batteryLevel);
        locationId = getIntent().getStringExtra("locationId");
    }

    private void setupListeners() {
        Button backButton = findViewById(R.id.backButton);
        Button addSlotButton = findViewById(R.id.addSlotButton);

        backButton.setOnClickListener(v -> finish());
        addSlotButton.setOnClickListener(v -> handleAddSlot());
    }

    private void handleAddSlot() {
        if (!validateInputs()) {
            return;
        }

        ParkingSlot slot = createParkingSlot();
        parkingSlotManager.addSlotToLocation(locationId, FirebaseAuthSingleton.getInstance().getUid(), this, slot, slot.getSensor());
        finish();
    }

    private boolean validateInputs() {
        if (!AddSlotValidator.isSlotIdValid(slotIdEditText, getString(R.string.slot_id_required))) {
            return false;
        }
        return AddSlotValidator.isBatteryLevelValid(
                batteryLevelEditText,
                getString(R.string.battery_level_required),
                getString(R.string.invalid_battery_level),
                getString(R.string.battery_level_range_error)
        );
    }

    private ParkingSlot createParkingSlot() {
        String slotId = Objects.requireNonNull(slotIdEditText.getText()).toString().trim();
        String sensorType = sensorTypeSpinner.getSelectedItem().toString();
        float batteryLevel = Float.parseFloat(Objects.requireNonNull(batteryLevelEditText.getText()).toString().trim());

        String sensorId = getString(R.string.sensor) + System.currentTimeMillis();
        String lastUpdate = getFormattedCurrentDateTime();

        ParkingSensor sensor = new ParkingSensor(sensorId, lastUpdate, (int) batteryLevel, sensorType);
        return new ParkingSlot(slotId, sensor);
    }

    private String getFormattedCurrentDateTime() {
        return java.text.DateFormat.getDateTimeInstance(
                java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, Locale.getDefault()
        ).format(new java.util.Date());
    }
}
