package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleLocation;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AutocompleteSearchHelper;

public class AddLocationActivity extends AppCompatActivity {

    private EditText editLocationName;
    private EditText editPostalCode;
    private EditText editPrice;
    private EditText editLocationAddressName;
    private double latitude;
    private double longitude;
    private String locationAddress;
    private FirebaseAuth oAuth;
    private final ParkingLocationManager parkingLocationManager = new ParkingLocationManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        oAuth = FirebaseAuthSingleton.getInstance();
        initializeUI();
        initializeAutocomplete();
    }

    private void initializeUI() {
        Button confirmButton;
        Button cancelButton;
        editLocationName= findViewById(R.id.locationName);
        editPostalCode= findViewById(R.id.postal_code);
        editPrice = findViewById(R.id.price);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);
        editLocationAddressName = findViewById(R.id.locationAddressName);
        confirmButton.setOnClickListener(v -> onConfirmButtonClicked());
        cancelButton.setOnClickListener(v -> onCancelButtonClicked());
    }

    private void initializeAutocomplete() {
        AutocompleteSearchHelper.initializeAutocompleteSearch(
                (AutocompleteSupportFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment)),
                this,
                new AutocompleteSearchHelper.PlaceSelectionCallback() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        latitude = Objects.requireNonNull(place.getLocation()).latitude;
                        longitude = place.getLocation().longitude;
                        locationAddress = place.getFormattedAddress();
                        editLocationAddressName.setText(locationAddress);
                        editLocationName.setText(place.getDisplayName());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d("Autocomplete", "Error: " + errorMessage);
                    }
                }
        );
    }

    private void onConfirmButtonClicked() {

        // Validate location address
        if (!AddLocationValidator.isLocationAddressValid(locationAddress)) {
            showValidationError(R.string.please_select_a_location_using_the_search_bar);
            return;
        }

        // Validate location name
        if (!AddLocationValidator.isLocationNameValid(editLocationName.getText().toString())) {
            editLocationName.setError(getString(R.string.please_enter_the_location_name));
            return;
        }

        // Validate postal code
        if (!AddLocationValidator.isPostalCodeValid(editPostalCode.getText().toString())) {
            editPostalCode.setError(getString((R.string.please_enter_the_postal_code)));
            return;
        }

        // Validate price
        if (!AddLocationValidator.isPriceValid(String.valueOf(editPrice.getText().toString()))) {
            editPrice.setError(getString(R.string.invalid_price_format));
            return;
        }

        // If all validations pass, proceed with adding the location to the database
        addParkingLocationToDatabase();
    }

    private void showValidationError(int errorMessageResId) {
        Toast.makeText(this, errorMessageResId, Toast.LENGTH_SHORT).show();
    }


    private void addParkingLocationToDatabase() {
        String locationNameStr = editLocationName.getText().toString().trim();
        String postalCodeStr = editPostalCode.getText().toString().trim();
        double priceValue = Double.parseDouble(editPrice.getText().toString().trim());

        ParkingLocation newLocation = new ParkingLocation(
                null, oAuth.getUid() ,null, postalCodeStr, locationNameStr, longitude, latitude, locationAddress, priceValue);

        parkingLocationManager.addParkingLocation(this, FirebaseAuthSingleton.getInstance().getUid(), newLocation);
        clearForm();
        finish();

    }

    private void clearForm() {
        editLocationName.setText("");
        editPostalCode.setText("");
        editPrice.setText("");
        editLocationAddressName.setText("");
        locationAddress = null;
        latitude = 0.0;
        longitude = 0.0;
    }

    private void onCancelButtonClicked() {
        finish();
    }
}
