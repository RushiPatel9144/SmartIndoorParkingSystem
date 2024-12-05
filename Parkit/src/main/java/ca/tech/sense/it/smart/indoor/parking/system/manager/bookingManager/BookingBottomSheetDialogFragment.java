package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;


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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.booking.PaymentActivity;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.favoriteManager.FavoritesManager;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.SlotAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.currency.Currency;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyPreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingUtils;
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
    private final ParkingLocationManager parkingLocationManager = new ParkingLocationManager();
    private ExecutorService executorService;
    private String ownerId;
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
        firebaseAuth = FirebaseAuthSingleton.getInstance();

        // Set up the slot spinner
        setupProceedToPaymentButton();
        setupCancelButton();
        setupSelectDateButton();

        // Fetch the parking location data when the dialog is opened
        fetchParkingLocationData();

        // Create an instance of FavoritesManager and setup the star button
        FavoritesManager favoritesManager = new FavoritesManager(getContext(), firebaseAuth, starButton, locationId,
                addressText, postalCodeText, bookingManager);
        favoritesManager.setupStarButton();

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
                    ownerId = location.getOwnerId();
                    titleTextView.setText(location.getName());
                    addressText.setText(location.getAddress());
                    postalCodeText.setText(location.getPostalCode());
                    setupSlotSpinnerData(location.getSlots(), locationId, selectedDate, selectedHour, bookingManager);
                    bookingManager.getSlotService().fetchPrice(locationId, price -> displayConvertedPrice(price));
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
        if (isAdded()) {
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
    }

    private void setupSlotSpinnerData(Map<String, ParkingSlot> slots, String locationId, String selectedDate, String selectedHour, BookingManager bookingManager) {
        if (slots == null || slots.isEmpty() || slotSpinner == null) {
            return; // Exit the method if any critical component is null
        }

        List<String> slotNames = new ArrayList<>();
        for (Map.Entry<String, ParkingSlot> entry : slots.entrySet()) {
            ParkingSlot slot = entry.getValue();
            if (slot != null && slot.getId() != null) {
                // Sanitize slot ID before adding it to the list
                String sanitizedSlotId = sanitizeSlotId(slot.getId());
                slotNames.add(sanitizedSlotId);
            } else {
                // Log or handle null values for debugging
                Log.e("setupSlotSpinnerData", "Null slot or slot ID encountered in slots map.");
            }
        }

        // Only set the adapter if slotNames is not empty
        if (!slotNames.isEmpty() && isAdded()) {
            SlotAdapter adapter = new SlotAdapter(requireContext(), android.R.layout.simple_spinner_item, slotNames, locationId, selectedDate, selectedHour, bookingManager);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            slotSpinner.setAdapter(adapter);
        }
    }

    // Add the sanitizeSlotId method here
    private String sanitizeSlotId(String slotId) {
        return slotId.replaceAll("[.#$\\[\\]]", "_"); // Replace invalid characters with '_'
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
                Log.d("BookingBottomSheet", "Selected Hour Changed: " + selectedHour);
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

                bookingManager.getSlotService().checkSlotAvailability(locationId, selectedSlot, selectedDate, selectedHour, status -> {
                    if ("occupied".equals(status)) {
                        Toast.makeText(requireContext(), R.string.slot_already_occupied, Toast.LENGTH_SHORT).show();
                    } else {
                        // Create a Booking object with the selected details
                        Booking booking = new Booking(
                                null,
                                titleTextView.getText().toString(),
                                BookingUtils.convertToMillis(selectedDate + " " + selectedTimeSlot.split(" - ")[0]),
                                BookingUtils.convertToMillis(selectedDate + " " + selectedTimeSlot.split(" - ")[1]),
                                addressText.getText().toString(),
                                postalCodeText.getText().toString(),
                                0,
                                convertedPrice,
                                selectedCurrency.getCode(),
                                selectedCurrency.getSymbol(),
                                selectedSlot,
                                BookingUtils.generatePassKey(), // Generate the pass key
                                locationId, // Add the locationId to the booking
                                null
                        );

                        // Create an Intent to start PaymentActivity and pass the booking data
                        Intent intent = new Intent(requireContext(), PaymentActivity.class);
                        intent.putExtra("booking", booking); // Pass the Booking object
                        intent.putExtra("ownerId", ownerId);
                        startActivity(intent);
                        dismiss();
                    }
                }, error -> Toast.makeText(requireContext(), R.string.error_checking_slot_availability, Toast.LENGTH_SHORT).show());
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
    // Method to set error message
    public void setErrorMessage(String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }
}

