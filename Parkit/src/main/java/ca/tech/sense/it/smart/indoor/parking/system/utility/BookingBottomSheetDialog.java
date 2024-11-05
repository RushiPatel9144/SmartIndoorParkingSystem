package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.BookingStatus;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private Context context;
    private Spinner slotSpinner, timeSlotSpinner;
    private Button confirmButton, cancelButton, selectDateButton;
    private ProgressBar progressBar;
    private TextView addressText, postalCodeText, errorTextView, priceTag, confirmationSummary;

    private String locationId;
    private ParkingUtility parkingUtility;

    public BookingBottomSheetDialog(@NonNull Context context, String locationId) {
        super(context);
        this.context = context;
        this.locationId = locationId; // Set the location ID directly from the constructor
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
        errorTextView = view.findViewById(R.id.error_text_view);
        priceTag = view.findViewById(R.id.priceTag);
        confirmationSummary = view.findViewById(R.id.confirmationSummary);

         // Set up the slot spinner
        setupTimeSlots(); // Set up the time slots spinner
        setupConfirmButton(); // Set up the confirm button
        setupCancelButton(); // Set up the cancel button
        setupSelectDateButton(); // Set up the date selection button

        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();
    }

    private void fetchParkingLocationData() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar while fetching

        parkingUtility.fetchParkingLocation(locationId, new ParkingUtility.FetchLocationCallback() {
            @Override
            public void onFetchSuccess(ParkingLocation location) {
                if (location != null) {
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                    priceTag.setText("Price: $" + location.getPrice());
                    setupSlotSpinnerData(location.getSlots()); // Setup slots based on the fetched location
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

    private void setupSlotSpinnerData(Map<String, ParkingSlot> slots) {
        List<String> slotNames = new ArrayList<>();
        for (Map.Entry<String, ParkingSlot> entry : slots.entrySet()) {
            slotNames.add(entry.getValue().getId()); // Corrected from getid() to getId()
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, slotNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        slotSpinner.setAdapter(adapter);
    }

    private void setupTimeSlots() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("timeSlots");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> timeSlots = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String timeSlot = snapshot.getValue(String.class);
                    if (timeSlot != null) {
                        timeSlots.add(timeSlot);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, timeSlots);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeSlotSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setErrorMessage("Failed to fetch time slots: " + databaseError.getMessage());
            }
        });
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            String selectedSlot = slotSpinner.getSelectedItem() != null ? slotSpinner.getSelectedItem().toString() : null;
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem() != null ? timeSlotSpinner.getSelectedItem().toString() : null;

            if (selectedSlot != null && selectedTimeSlot != null) {
                setupProceedToPayment(selectedSlot, selectedTimeSlot);
            } else {
                Toast.makeText(context, "Please select a slot and time.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> dismiss()); // Dismiss the bottom sheet
    }

    private void setupSelectDateButton() {
        selectDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                confirmationSummary.setText("Selected Date: " + selectedDate); // Update summary with selected date
            }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupProceedToPayment(String slot, String timing) {
        // Placeholder for payment processing logic
        confirmationSummary.setText(String.format("Summary: Date: TBD, Time: %s, Slot: %s, Price: %s", timing, slot, priceTag.getText()));

        // Optionally, update booking status in the relevant parking slot
        // For example, assuming you have the date, you would save the booking status
        // parkingUtility.updateBookingStatus(locationId, slot, selectedDate, new BookingStatus("occupied", selectedDate));
    }

    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }
}
