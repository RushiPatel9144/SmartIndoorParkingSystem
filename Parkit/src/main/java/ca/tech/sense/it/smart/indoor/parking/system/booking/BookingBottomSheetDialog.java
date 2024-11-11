package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;

import android.os.Bundle;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private final Context context;
    private Spinner slotSpinner, timeSlotSpinner;
    private Button confirmButton, cancelButton;
    private ProgressBar progressBar;
    private TextView addressText, postalCodeText, errorTextView, selectedDateTextview, priceTextView;
    private ImageButton selectDateButton, starButton;

    private final String locationId;
    private String selectedDate;
    private final BookingManager bookingManager;

    // Constructor with dependency injection
    public BookingBottomSheetDialog(@NonNull Context context, String locationId, BookingManager bookingManager) {
        super(context);
        this.context = context;
        this.locationId = locationId;
        this.bookingManager = bookingManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_booking, null);
        setContentView(view);

        // Initialize UI elements
        initializeUIElements(view);

        // Set up the slot spinner
        setupSlotSpinnerData(new HashMap<>());
        setupConfirmButton();
        setupCancelButton();
        setupSelectDateButton();
        setupStarButton();

        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();

        // Set up the time slots with the current date as default
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        setupTimeSlots(selectedDate);
    }

    // Method to initialize UI elements
    private void initializeUIElements(View view) {
        slotSpinner = view.findViewById(R.id.slotSpinner);
        timeSlotSpinner = view.findViewById(R.id.timeSlotSpinner);
        confirmButton = view.findViewById(R.id.confirmButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        selectDateButton = view.findViewById(R.id.selectDateButton);
        starButton = view.findViewById(R.id.iv_add_to_favorites);
        progressBar = view.findViewById(R.id.progressBar);
        addressText = view.findViewById(R.id.addressText);
        postalCodeText = view.findViewById(R.id.postalCodeText);
        selectedDateTextview = view.findViewById(R.id.selectedDate);
        priceTextView = view.findViewById(R.id.priceTag);
    }

    // Method to fetch parking location data from Firebase
    private void fetchParkingLocationData() {
        progressBar.setVisibility(View.VISIBLE);

        bookingManager.fetchParkingLocation(locationId, new BookingManager.FetchLocationCallback() {
            @Override
            public void onFetchSuccess(ParkingLocation location) {
                if (location != null) {
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                    setupSlotSpinnerData(location.getSlots());
                    bookingManager.fetchPrice(locationId, price -> priceTextView.setText(String.format(Locale.getDefault(), "Price: $%.2f", price)));
                } else {
                    setErrorMessage(context.getString(R.string.location_data_is_not_available));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFetchFailure(Exception exception) {
                setErrorMessage(String.format("%s%s", context.getString(R.string.failed_to_fetch_location_data), exception.getMessage()));
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Method to set up slot spinner data
    private void setupSlotSpinnerData(Map<String, ParkingSlot> slots) {
        List<String> slotNames = new ArrayList<>();
        for (Map.Entry<String, ParkingSlot> entry : slots.entrySet()) {
            slotNames.add(entry.getValue().getId());
        }

        // Check if slotNames is not null and not empty
        if (slotNames != null && !slotNames.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, slotNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            slotSpinner.setAdapter(adapter);
        }
    }


    // Method to set up time slots based on the selected date
    private void setupTimeSlots(String selectedDate) {
        List<String> timeSlots = generateTimeSlots(selectedDate);
        if (timeSlots.isEmpty()) {
            timeSlots.add("Choose another date");
            confirmButton.setEnabled(false);
        } else {
            confirmButton.setEnabled(true);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);
    }

    // Method to generate time slots for the selected date
    private List<String> generateTimeSlots(String selectedDate) {
        List<String> timeSlots = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        // Parse the selected date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar selectedCalendar = Calendar.getInstance();
        try {
            Date date = sdf.parse(selectedDate);
            if (date != null) {
                selectedCalendar.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int hour = 0; hour < 24; hour++) {
            String startTime = String.format(Locale.getDefault(), "%02d:00", hour);
            String endTime = String.format(Locale.getDefault(), "%02d:00", (hour + 1) % 24);

            // Create a calendar instance for the start time
            Calendar slotTime = (Calendar) selectedCalendar.clone();
            slotTime.set(Calendar.HOUR_OF_DAY, hour);
            slotTime.set(Calendar.MINUTE, 0);
            slotTime.set(Calendar.SECOND, 0);
            slotTime.set(Calendar.MILLISECOND, 0);

            // Add the time slot if it is in the future
            if (slotTime.after(now)) {
                timeSlots.add(startTime + " - " + endTime);
            }
        }
        return timeSlots;
    }

    // Method to set up the confirm button
    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            String selectedSlot = slotSpinner.getSelectedItem() != null ? slotSpinner.getSelectedItem().toString() : null;
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem() != null ? timeSlotSpinner.getSelectedItem().toString() : null;

            if (selectedSlot != null && selectedTimeSlot != null && selectedDate != null) {
                bookingManager.confirmBooking(locationId, selectedSlot, selectedTimeSlot, selectedDate, addressText.getText().toString(), () -> {
                    Toast.makeText(context, R.string.booking_confirmed_and_saved, Toast.LENGTH_SHORT).show();
                    dismiss();
                }, error -> Toast.makeText(context, context.getString(R.string.failed_to_save_booking) + error.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, R.string.please_select_a_slot_date_and_time, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to set up the cancel button
    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> dismiss());
    }

    // Method to set up the select date button
    private void setupSelectDateButton() {
        selectDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                selectedDateTextview.setText("Selected Date: " + selectedDate);
                setupTimeSlots(selectedDate); // Update time slots based on the selected date
            }, year, month, day);

            // Set the minimum date to today
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });
    }

    // Method to set up the star button for saving location to favorites
    private void setupStarButton() {
        starButton.setOnClickListener(v -> {
            String address = addressText.getText().toString();
            bookingManager.saveLocationToFavorites(locationId, address, () -> Toast.makeText(context, R.string.location_saved_to_favorites, Toast.LENGTH_SHORT).show(), error -> Toast.makeText(context, context.getString(R.string.failed_to_save_location) + error.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // Method to set error message
    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }
}
