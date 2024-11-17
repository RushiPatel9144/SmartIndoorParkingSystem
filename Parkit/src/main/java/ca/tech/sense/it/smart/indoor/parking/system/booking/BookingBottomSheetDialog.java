package ca.tech.sense.it.smart.indoor.parking.system.booking;

import static androidx.core.content.ContextCompat.getColor;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;

import android.os.Bundle;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import ca.tech.sense.it.smart.indoor.parking.system.payment.PaymentBottomSheetDialog;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private final Context context;
    private Spinner slotSpinner, timeSlotSpinner;
    private Button proceedToPaymentButton, cancelButton;
    private ProgressBar progressBar;
    private TextView addressText, postalCodeText, errorTextView, selectedDateTextview, priceTextView;
    private ImageButton selectDateButton, starButton;

    private final String locationId;
    private String selectedDate;
    private final BookingManager bookingManager;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

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

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Set up the slot spinner
        setupSlotSpinnerData(new HashMap<>());
        setupProceedToPaymentButton();
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
        proceedToPaymentButton = view.findViewById(R.id.proceedToPaymentButton);
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
        if (slots == null || slots.isEmpty() || slotSpinner == null || context == null) {
            return; // Exit the method if any critical component is null
        }

        List<String> slotNames = new ArrayList<>();
        for (Map.Entry<String, ParkingSlot> entry : slots.entrySet()) {
            ParkingSlot slot = entry.getValue();
            if (slot != null && slot.getId() != null) {
                slotNames.add(slot.getId());
            } else {
                // Log or handle null values for debugging
                Log.e("setupSlotSpinnerData", "Null slot or slot ID encountered in slots map.");
            }
        }

        // Only set the adapter if slotNames is not empty
        if (!slotNames.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, slotNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            slotSpinner.setAdapter(adapter);
        }
    }

    // Method to set up time slots based on the selected date
    private void setupTimeSlots(String selectedDate) {
        List<String> timeSlots = generateTimeSlots(selectedDate);
        if (timeSlots.isEmpty()) {
            timeSlots.add(context.getString(R.string.choose_another_date));
            proceedToPaymentButton.setEnabled(false);
        } else {
            proceedToPaymentButton.setEnabled(true);
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

    // Method to set up the proceed to payment button
    private void setupProceedToPaymentButton() {
        proceedToPaymentButton.setOnClickListener(v -> {
            String selectedSlot = slotSpinner.getSelectedItem() != null ? slotSpinner.getSelectedItem().toString() : null;
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem() != null ? timeSlotSpinner.getSelectedItem().toString() : null;

            if (selectedSlot != null && selectedTimeSlot != null && selectedDate != null) {
                // Create a Booking object with the selected details
                Booking booking = new Booking(
                        "Park It", // Use "Park It" as title
                        convertToMillis(selectedDate + " " + selectedTimeSlot.split(" - ")[0]),
                        convertToMillis(selectedDate + " " + selectedTimeSlot.split(" - ")[1]),
                        addressText.getText().toString(),
                        Double.parseDouble(priceTextView.getText().toString().replace("Price: $", "")),
                        selectedSlot,
                        bookingManager.generatePassKey(), // Generate the pass key
                        locationId // Add the locationId to the booking
                );

                // Open the PaymentBottomSheetDialog
                PaymentBottomSheetDialog paymentDialog = new PaymentBottomSheetDialog(context, booking, bookingManager, BookingBottomSheetDialog.this);
                paymentDialog.show();
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

    private void setupStarButton() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, R.string.user_not_authenticated, Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userFavoritesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                .child("saved_locations")
                .child(locationId);

        // Check if the location is already in the favorites
        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Location is already in favorites, set star button to green
                    starButton.setColorFilter(ContextCompat.getColor(context, R.color.logo));
                } else {
                    // Location is not in favorites, set star button to black
                    starButton.setColorFilter(ContextCompat.getColor(context, R.color.black));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check favorites"+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for changes in the saved_locations database
        userFavoritesRef.getParent().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals(locationId)) {
                    starButton.setColorFilter(ContextCompat.getColor(context, R.color.logo));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals(locationId)) {
                    starButton.setColorFilter(ContextCompat.getColor(context, R.color.black));
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to listen for changes" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        starButton.setOnClickListener(v -> {
            userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Location is already in favorites, remove it
                        userFavoritesRef.removeValue().addOnSuccessListener(aVoid -> {
                            starButton.setColorFilter(ContextCompat.getColor(context, R.color.black));
                            Toast.makeText(context, "Location removed from favorites", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(error -> {
                            Toast.makeText(context, "Failed to remove location" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Location is not in favorites, add it
                        String address = addressText.getText().toString();
                        String postalCode = postalCodeText.getText().toString();

                        // Fetch the name from the database
                        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("parkingLocations").child(locationId);
                        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String name = snapshot.child("name").getValue(String.class);

                                if (name != null) {
                                    bookingManager.saveLocationToFavorites(locationId, address, postalCode, name, () -> {
                                        starButton.setColorFilter(getColor(context, R.color.logo));
                                        Toast.makeText(context, R.string.location_saved_to_favorites, Toast.LENGTH_SHORT).show();
                                    }, error -> {
                                        Toast.makeText(context, context.getString(R.string.failed_to_save_location) + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Toast.makeText(context, "Failed to fetch the name", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Failed to fetch the name" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to check favorites" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }




    // Method to set error message
    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    // Helper method to convert date and time to milliseconds
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
}
