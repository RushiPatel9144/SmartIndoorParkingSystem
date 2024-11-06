package ca.tech.sense.it.smart.indoor.parking.system.utility;

import static ca.tech.sense.it.smart.indoor.parking.system.R.string.booking_confirmed_and_saved;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.please_select_a_slot_date_and_time;

import android.annotation.SuppressLint;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.BookingDetails;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private Context context;
    private Spinner slotSpinner, timeSlotSpinner;
    private Button confirmButton, cancelButton;
    private ProgressBar progressBar;
    private TextView addressText, postalCodeText, errorTextView, selectedDateTextview;

    private ImageButton selectDateButton;
    private String locationId;
    private String selectedDate;
    private ParkingUtility parkingUtility;


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
        selectedDateTextview = view.findViewById(R.id.selectedDate);

        // Set up the slot spinner
        setupTimeSlots();
        setupConfirmButton();
        setupCancelButton();
        setupSelectDateButton();

        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();
    }

    private void fetchParkingLocationData() {
        progressBar.setVisibility(View.VISIBLE);

        parkingUtility.fetchParkingLocation(locationId, new ParkingUtility.FetchLocationCallback() {
            @Override
            public void onFetchSuccess(ParkingLocation location) {
                if (location != null) {
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                    setupSlotSpinnerData(location.getSlots());
                } else setErrorMessage(context.getString(R.string.location_data_is_not_available));
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFetchFailure(Exception exception) {
                setErrorMessage(String.format("%s%s", context.getString(R.string.failed_to_fetch_location_data), exception.getMessage()));
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
                Toast.makeText(context, please_select_a_slot_date_and_time, Toast.LENGTH_SHORT).show();
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

            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Format the selected date
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                // Update the TextView with the selected date
                selectedDateTextview.setText("Selected Date: " + selectedDate);
            }, year, month, day);

            datePickerDialog.show();

        });
    }

    private void confirmBooking(String slot, String timing) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        if (userName == null) {
            userName = "";
        }

        // Convert timing to start and end time in milliseconds
        String[] times = timing.split(" - ");
        long startTime = convertToMillis(selectedDate + " " + times[0]);
        long endTime = convertToMillis(selectedDate + " " + times[1]);

        double price = 25.00; // Replace with actual price

        Booking booking = new Booking(
                "Booking Title", // Replace with actual title
                startTime,
                endTime,
                addressText.getText().toString(),
                slot,
                price
        );

        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("bookings")
                .child(userId)
                .push();

        databaseRef.setValue(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, R.string.booking_confirmed_and_saved, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, context.getString(R.string.failed_to_save_booking) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private long convertToMillis(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }




    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

}
