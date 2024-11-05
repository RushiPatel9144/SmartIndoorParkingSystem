package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private Context context;
    private Spinner timeSlotSpinner;
    private Button confirmButton, cancelButton;
    private ProgressBar progressBar;
    private TextView addressText;
    private TextView postalCodeText;
    private TextView errorTextView;;


    private String locationId; // Store location ID
    private ParkingUtility parkingUtility;

    public BookingBottomSheetDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        parkingUtility = new ParkingUtility(); // Initialize ParkingUtility
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_booking, null);
        setContentView(view);

        timeSlotSpinner = view.findViewById(R.id.timeSlotSpinner);
        confirmButton = view.findViewById(R.id.confirmButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        progressBar = view.findViewById(R.id.progressBar);
        addressText = view.findViewById(R.id.addressText);
        postalCodeText = view.findViewById(R.id.postalCodeText);
        errorTextView = view.findViewById(R.id.error_text_view);

        // Fetch the location ID from the previous activity/fragment or set it directly
        locationId = "-OAv5qyk1zAkXPLmrMXp"; // Replace this with actual location ID

        setupTimeSlots();
        setupConfirmButton();
        setupCancelButton();

        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
        fetchLocationDetails(); // Fetch the details when the location ID is set
    }

    private void fetchLocationDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("parkingLocations").child(locationId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ParkingLocation location = dataSnapshot.getValue(ParkingLocation.class);
                    if (location != null) {
                        addressText.setText(location.getAddress());
                        // Populate other fields like slots, price, etc.
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }


    private void fetchParkingLocationData() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar while fetching

        parkingUtility.fetchParkingLocation(locationId, new ParkingUtility.FetchLocationCallback() {
            @Override
            public void onFetchSuccess(ParkingLocation location) {
                if (location != null) {
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                } else {
                    setErrorMessage("Location data is not available.");
                }
                progressBar.setVisibility(View.GONE); // Hide progress bar
            }

            @Override
            public void onFetchFailure(Exception exception) {
                setErrorMessage("Failed to fetch location data: " + exception.getMessage());
                progressBar.setVisibility(View.GONE); // Hide progress bar
            }
        });
    }


    public void setParkingLocation(ParkingLocation location) {
        if (location != null) {
            if (addressText != null) {
                addressText.setText(location.getAddress()); // Set the address
            }
            if (postalCodeText != null) {
                postalCodeText.setText(location.getPostalCode()); // Set the postal code
            }
        } else {
            setErrorMessage("Invalid parking location data.");
        }
    }

        private void setupTimeSlots() {
        // Populate spinner with time slots
        String[] timeSlots = {"9:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM", "1:00 PM - 2:00 PM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem().toString();
            if (!selectedTimeSlot.isEmpty()) {
                setupProceedToPayment(selectedTimeSlot);
            } else {
                Toast.makeText(context, "Please select a time slot.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> dismiss()); // Dismiss the bottom sheet
    }

    private void setupProceedToPayment(String timing) {
        // Placeholder for payment processing logic
    }


    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }
}
