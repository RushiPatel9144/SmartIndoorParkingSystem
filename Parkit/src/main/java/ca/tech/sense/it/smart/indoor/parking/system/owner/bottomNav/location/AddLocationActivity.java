package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AutocompleteSearchHelper;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;

public class AddLocationActivity extends AppCompatActivity {

    private EditText locationName;
    private EditText postalCode;
    private EditText price;
    private EditText locationAddressName;
    private Button confirmButton;
    private Button cancelButton;
    private double latitude;
    private double longitude;
    private String locationAddress;
    private String parkingLocationName;
    private ParkingUtility parkingUtility = new ParkingUtility();
    private FirebaseAuth oAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location); // You should create an activity layout file for this

        oAuth = FirebaseAuthSingleton.getInstance();
        initializeUI();
        initializeAutocomplete();
    }

    private void initializeUI() {
        locationName = findViewById(R.id.locationName);
        postalCode = findViewById(R.id.postal_code);
        price = findViewById(R.id.price);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);
        locationAddressName = findViewById(R.id.locationAddressName);

        confirmButton.setOnClickListener(v -> onConfirmButtonClicked());
        cancelButton.setOnClickListener(v -> onCancelButtonClicked());
    }

    private void initializeAutocomplete() {
        AutocompleteSearchHelper.initializeAutocompleteSearch(
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment),
                this,
                new AutocompleteSearchHelper.PlaceSelectionCallback() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        handlePlace(place);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d("Autocomplete", getString(R.string.error) + errorMessage);
                    }
                }
        );
    }

    public void handlePlace(Place place){
        latitude = Objects.requireNonNull(place.getLocation()).latitude;
        longitude = Objects.requireNonNull(place.getLocation()).longitude;
        parkingLocationName = place.getDisplayName();
        locationAddress = place.getFormattedAddress();
        locationAddressName.setText(locationAddress);
        locationName.setText(parkingLocationName);
    }

    private void onConfirmButtonClicked() {
        if (!isLocationNameValid()) return;
        if (!isPostalCodeValid()) return;
        if (!isPriceValid()) return;
        if (!isLocationAddressValid()) return;

        addParkingLocationToDatabase();
        clearForm();
        finish();
    }

    private boolean isLocationNameValid() {
        String locationNameStr = locationName.getText().toString().trim();
        if (locationNameStr.isEmpty()) {
            showToast(getString(R.string.please_enter_the_location_name));
            locationName.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isPostalCodeValid() {
        String postalCodeStr = postalCode.getText().toString().trim();
        if (postalCodeStr.isEmpty()) {
            showToast(getString(R.string.please_enter_the_postal_code));
            postalCode.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isPriceValid() {
        String priceStr = price.getText().toString().trim();
        if (priceStr.isEmpty()) {
            showToast(getString(R.string.please_enter_the_price));
            price.requestFocus();
            return false;
        }

        double priceValue;
        try {
            priceValue = Double.parseDouble(priceStr);
            if (priceValue < 0.00) {
                showToast(getString(R.string.price_must_be_a_positive_value));
                price.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showToast(getString(R.string.invalid_price_format));
            price.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isLocationAddressValid() {
        if (locationAddress == null || locationAddress.isEmpty()) {
            showToast(getString(R.string.please_select_a_location_using_the_search_bar));
            return false;
        }
        return true;
    }

    private void addParkingLocationToDatabase() {
        String locationNameStr = locationName.getText().toString().trim();
        String postalCodeStr = postalCode.getText().toString().trim();
        String priceStr = price.getText().toString().trim();

        double priceValue = Double.parseDouble(priceStr);

        ParkingLocation newLocation = new ParkingLocation(
                null,
                null,
                postalCodeStr,
                locationNameStr,
                longitude,
                latitude,
                locationAddress,
                priceValue
        );
        parkingUtility.addParkingLocation(this, oAuth.getUid(), newLocation);
    }

    private void clearForm() {
        locationName.setText("");
        postalCode.setText("");
        price.setText("");
        locationAddress = null;
        latitude = 0.0;
        longitude = 0.0;
        locationAddressName = null;
    }

    private void onCancelButtonClicked() {
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
