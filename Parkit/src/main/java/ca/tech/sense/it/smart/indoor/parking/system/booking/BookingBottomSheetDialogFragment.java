package ca.tech.sense.it.smart.indoor.parking.system.booking;

import static androidx.core.content.ContextCompat.getColor;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.currency.Currency;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyPreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingInterface;

public class BookingBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private final Context context;
    private Spinner slotSpinner;
    private Spinner timeSlotSpinner;
    private Button proceedToPaymentButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView addressText;
    private TextView postalCodeText;
    private TextView selectedDateTextview;
    private TextView priceTextView;
    private TextView errorTextView;
    private TextView titleTextView;
    private ImageButton selectDateButton;
    private ImageButton starButton;
    private final String locationId;
    private String selectedDate;
    private String selectedHour;
    private final BookingManager bookingManager;
    private double convertedPrice;
    private Currency selectedCurrency;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private final ParkingLocationManager parkingLocationManager = new ParkingLocationManager();
    private ExecutorService executorService;

    private ParkingLocation location; // Define the ParkingLocation variable
    // Constructor with dependency injection
    public BookingBottomSheetDialogFragment(ExecutorService executorService, String locationId, BookingManager bookingManager, Context context) {
        this.locationId = locationId;
        this.bookingManager = bookingManager;
        this.context = context;
        this.executorService = executorService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_booking_bottom_sheet_dialog, container, false);

        // Initialize Firebase database reference
        FirebaseDatabaseSingleton.getInstance().getReference();

        // Initialize UI elements
        initializeUIElements(view);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Set up the slot spinner
        setupProceedToPaymentButton();
        setupCancelButton();
        setupSelectDateButton();
        setupStarButton();

        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();

        // Set up the time slots with the current date as default
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        setupTimeSlots(selectedDate);

        return view;
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
        titleTextView = view.findViewById(R.id.addressTitle);
    }


    // Method to fetch parking location data from Firebase
    private void fetchParkingLocationData() {
        progressBar.setVisibility(View.VISIBLE);

        parkingLocationManager.fetchParkingLocation(context,executorService,locationId, new ParkingInterface.FetchLocationCallback() {
            @Override
            public void onFetchSuccess(ParkingLocation fetchedLocation) {
                if (fetchedLocation != null) {
                    location = fetchedLocation; // Assign the fetched location to the variable
                    titleTextView.setText(location.getName());
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                    setupSlotSpinnerData(location.getSlots(), locationId, selectedDate, selectedHour, bookingManager);
                    bookingManager.fetchPrice(locationId, price -> displayConvertedPrice(price));
                } else {
                    setErrorMessage(requireContext().getString(R.string.location_data_is_not_available));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFetchFailure(Exception exception) {
                setErrorMessage(String.format("%s%s", requireContext().getString(R.string.failed_to_fetch_location_data), exception.getMessage()));
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Method to convert currency
    private void displayConvertedPrice(double priceInCad) {
        CurrencyPreferenceManager currencyPreferenceManager = new CurrencyPreferenceManager(requireContext());
        String selectedCurrencyCode = currencyPreferenceManager.getSelectedCurrency();
        selectedCurrency = CurrencyManager.getInstance().getCurrency(selectedCurrencyCode);

        if (selectedCurrency != null) {
            convertedPrice = CurrencyManager.getInstance().convertFromCAD(priceInCad, selectedCurrencyCode);
            priceTextView.setText(String.format(Locale.getDefault(), "Price: %s %.2f", selectedCurrency.getSymbol(), convertedPrice));
        } else {
            priceTextView.setText(String.format(Locale.getDefault(), "Price: %s %.2f", "CAD$", priceInCad));
        }
    }

    // Method to set up slot spinner data
    private void setupSlotSpinnerData(Map<String, ParkingSlot> slots, String locationId, String selectedDate, String selectedHour, BookingManager bookingManager) {
        if (slots == null || slots.isEmpty() || slotSpinner == null) {
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
            SlotAdapter adapter = new SlotAdapter(requireContext(), android.R.layout.simple_spinner_item, slotNames, locationId, selectedDate, selectedHour, bookingManager);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            slotSpinner.setAdapter(adapter);
        }
    }

    // Method to set up time slots based on the selected date
    private void setupTimeSlots(String selectedDate) {
        List<String> timeSlots = generateTimeSlots(selectedDate);
        if (timeSlots.isEmpty()) {
            timeSlots.add(requireContext().getString(R.string.choose_another_date));
            proceedToPaymentButton.setEnabled(false);
        } else {
            proceedToPaymentButton.setEnabled(true);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);

        // Set the selectedHour based on the first available time slot
        if (!timeSlots.isEmpty()) {
            selectedHour = timeSlots.get(0).split(" - ")[0];
            Log.d("BookingBottomSheet", "Selected Hour: " + selectedHour); // Add logging
        }

        // Add a listener to update the selectedHour when the user selects a different time slot
        timeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHour = timeSlots.get(position).split(" - ")[0];
                Log.d("BookingBottomSheet", "Selected Hour Changed: " + selectedHour); // Add logging
                // Update the slot spinner data based on the new selected hour
                if (location != null) {
                    setupSlotSpinnerData(location.getSlots(), locationId, selectedDate, selectedHour, bookingManager);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
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
                String selectedHour = selectedTimeSlot.split(" - ")[0];

                bookingManager.checkSlotAvailability(locationId, selectedSlot, selectedDate, selectedHour, status -> {
                    if ("occupied".equals(status)) {
                        Toast.makeText(requireContext(), R.string.slot_already_occupied, Toast.LENGTH_SHORT).show();
                    } else {
                        // Create a Booking object with the selected details
                        Booking booking = new Booking(
                                titleTextView.getText().toString(),
                                convertToMillis(selectedDate + " " + selectedTimeSlot.split(" - ")[0]),
                                convertToMillis(selectedDate + " " + selectedTimeSlot.split(" - ")[1]),
                                addressText.getText().toString(),
                                postalCodeText.getText().toString(),
                                convertedPrice,
                                selectedCurrency.getCode(),
                                selectedCurrency.getSymbol(),
                                selectedSlot,
                                bookingManager.generatePassKey(), // Generate the pass key
                                locationId // Add the locationId to the booking
                        );

                        // Create an Intent to start PaymentActivity and pass the booking data
                        Intent intent = new Intent(requireContext(), PaymentActivity.class);
                        intent.putExtra("booking", booking); // Pass the Booking object
                        startActivity(intent);
                        dismiss();
                    }
                }, error -> {
                    Toast.makeText(requireContext(), R.string.error_checking_slot_availability, Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(requireContext(), R.string.please_select_a_slot_date_and_time, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to handle cancel button
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

    // Method to handle adding to favorites (optional feature)
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
                    } else {// Location is not in favorites, add it
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