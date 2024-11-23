package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation;

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
import ca.tech.sense.it.smart.indoor.parking.system.manager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AutocompleteSearchHelper;

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
    private FirebaseAuth oAuth;
    private ParkingLocationManager parkingLocationManager = new ParkingLocationManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
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
                        latitude = Objects.requireNonNull(place.getLocation()).latitude;
                        longitude = place.getLocation().longitude;
                        locationAddress = place.getFormattedAddress();
                        locationAddressName.setText(locationAddress);
                        locationName.setText(place.getDisplayName());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d("Autocomplete", "Error: " + errorMessage);
                    }
                }
        );
    }

    private void onConfirmButtonClicked() {
        if (!AddLocationValidator.isLocationNameValid(locationName, getString(R.string.please_enter_the_location_name)))
            return;
        if (!AddLocationValidator.isPostalCodeValid(postalCode, getString(R.string.please_enter_the_postal_code)))
            return;
        if (!AddLocationValidator.isPriceValid(
                price,
                getString(R.string.please_enter_the_price),
                getString(R.string.invalid_price_format),
                getString(R.string.price_must_be_a_positive_value)))
            return;
        if (!AddLocationValidator.isLocationAddressValid(locationAddress)) {
            Toast.makeText(this, R.string.please_select_a_location_using_the_search_bar, Toast.LENGTH_SHORT).show();
            return;
        }

        addParkingLocationToDatabase();
        clearForm();
        finish();
    }

    private void addParkingLocationToDatabase() {
        String locationNameStr = locationName.getText().toString().trim();
        String postalCodeStr = postalCode.getText().toString().trim();
        double priceValue = Double.parseDouble(price.getText().toString().trim());

        ParkingLocation newLocation = new ParkingLocation(
                null, oAuth.getUid() ,null, postalCodeStr, locationNameStr, longitude, latitude, locationAddress, priceValue);

        parkingLocationManager.addParkingLocation(this, FirebaseAuthSingleton.getInstance().getUid(), newLocation);
    }

    private void clearForm() {
        locationName.setText("");
        postalCode.setText("");
        price.setText("");
        locationAddressName.setText("");
        locationAddress = null;
        latitude = 0.0;
        longitude = 0.0;
    }

    private void onCancelButtonClicked() {
        finish();
    }
}
