package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private Context context;
    private Spinner slotSpinner, timeSlotSpinner;
    private Button confirmButton, cancelButton, selectDateButton;
    private ProgressBar progressBar;
    private TextView addressText, postalCodeText, errorTextView, priceTag, confirmationSummary;
    private ImageButton starButton;
    private String locationId;
    private String selectedDate;
    private ParkingUtility parkingUtility;
    private double basePrice = 25.00; // Base price in USD

    public BookingBottomSheetDialog(@NonNull Context context, String locationId) {
        super(context);
        this.context = context;
        this.locationId = locationId;
        parkingUtility = new ParkingUtility();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_booking, null);
        setContentView(view);

        // Initialize UI elements
        slotSpinner = view.findViewById(R.id.slotSpinner);
        timeSlotSpinner = view.findViewById(R.id.timeSlotSpinner);
        confirmButton = view.findViewById(R.id.confirmButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        selectDateButton = view.findViewById(R.id.selectDateButton);
        progressBar = view.findViewById(R.id.progressBar);
        addressText = view.findViewById(R.id.addressText);
        postalCodeText = view.findViewById(R.id.postalCodeText);
        priceTag = view.findViewById(R.id.priceTag);
        confirmationSummary = view.findViewById(R.id.confirmationSummary);
        starButton = view.findViewById(R.id.iv_add_to_favorites);
        // Set up the slot spinner
        setupTimeSlots();
        setupConfirmButton();
        setupCancelButton();
        setupSelectDateButton();
        updateCurrencyDisplay();
        setupStarButton();
        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();
    }


    private void updateCurrencyDisplay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String currency = preferences.getString("selected_currency", "USD");

        double convertedPrice;
        switch (currency) {
            case "INR":
                convertedPrice = basePrice * 75.0; // Conversion rate USD to INR
                priceTag.setText(String.format("Price: ₹%.2f", convertedPrice));
                break;
            case "CAD":
                convertedPrice = basePrice * 1.25; // Conversion rate USD to CAD
                priceTag.setText(String.format("Price: CAD$%.2f", convertedPrice));
                break;
            default: // USD
                priceTag.setText(String.format("Price: $%.2f", basePrice));
                break;
        }
    }

    private void fetchParkingLocationData() {
        progressBar.setVisibility(View.VISIBLE);

        parkingUtility.fetchParkingLocation(locationId, new ParkingUtility.FetchLocationCallback() {
            @Override
            public void onFetchSuccess(ParkingLocation location) {
                if (location != null) {
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                    priceTag.setText("Price: $" + location.getPrice());
                    setupSlotSpinnerData(location.getSlots());
                } else {
                    setErrorMessage("Location data is not available.");
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFetchFailure(Exception exception) {
                setErrorMessage("Failed to fetch location data: " + exception.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupSlotSpinnerData(Map<String, ParkingSlot> slots) {
        List<String> slotNames = new ArrayList<>();
        for (Map.Entry<String, ParkingSlot> entry : slots.entrySet()) {
            slotNames.add(entry.getValue().getId());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, slotNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        slotSpinner.setAdapter(adapter);
    }

    private void setupTimeSlots() {
        List<String> timeSlots = generateTimeSlots();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);
    }

    private List<String> generateTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            String startTime = String.format(Locale.getDefault(), "%02d:00", hour);
            String endTime = String.format(Locale.getDefault(), "%02d:00", (hour + 1) % 24);
            timeSlots.add(startTime + " - " + endTime);
        }
        return timeSlots;
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            String selectedSlot = slotSpinner.getSelectedItem() != null ? slotSpinner.getSelectedItem().toString() : null;
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem() != null ? timeSlotSpinner.getSelectedItem().toString() : null;

            if (selectedSlot != null && selectedTimeSlot != null && selectedDate != null) {
                confirmBooking(selectedSlot, selectedTimeSlot);
            } else {
                Toast.makeText(context, "Please select a slot, date, and time.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void setupSelectDateButton() {
        selectDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                updateConfirmationSummary();
            }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void updateConfirmationSummary() {
        confirmationSummary.setText(String.format("Summary: Date: %s, Time: TBD, Slot: TBD, Price: %s", selectedDate, priceTag.getText()));
    }

    private void confirmBooking(String slot, String timing) {
        confirmationSummary.setText(String.format("Summary: Date: %s, Time: %s, Slot: %s, Price: %s", selectedDate, timing, slot, priceTag.getText()));
        Toast.makeText(context, "Booking confirmed", Toast.LENGTH_SHORT).show();
    }

    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }
    private void setupStarButton() {
        starButton.setOnClickListener(v -> {
            // Get the current location details (you can customize this to fetch actual data)
            String locationId = this.locationId; // You can get this from your current instance or layout
            String address = addressText.getText().toString(); // Get the address from your TextView

            // Save the location to Firebase Realtime Database
            saveLocationToFavorites(locationId, address);
        });
    }
    private void saveLocationToFavorites(String locationId, String address) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get current user ID

        // Create a map to save location data
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("locationId", locationId);
        locationData.put("address", address);

        // Reference to Firebase Realtime Database for user's saved locations
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("saved_locations")
                .child(locationId);

        // Save the location data
        databaseRef.setValue(locationData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Location saved to favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to save location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
