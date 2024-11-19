package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import static androidx.media3.common.MediaLibraryInfo.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AutocompleteSearchHelper;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;

public class AddLocationFragment extends Fragment {

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

    public AddLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_add_location, container, false);
        oAuth = FirebaseAuthSingleton.getInstance();
        initializeUI(view);
        initializeAutocomplete();
        return view;
    }
    private void initializeUI(View rootView) {
        // Initialize Views
        locationName = rootView.findViewById(R.id.locationName);
        postalCode = rootView.findViewById(R.id.postal_code);
        price = rootView.findViewById(R.id.price);
        confirmButton = rootView.findViewById(R.id.confirmButton);
        cancelButton = rootView.findViewById(R.id.cancelButton);
        locationAddressName = rootView.findViewById(R.id.locationAddressName);
        confirmButton.setOnClickListener(v -> onConfirmButtonClicked());
        cancelButton.setOnClickListener(v -> onCancelButtonClicked());
    }

    private void initializeAutocomplete() {
        AutocompleteSearchHelper.initializeAutocompleteSearch(
                (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment),
                requireContext(),
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
         locationAddress = place.getShortFormattedAddress();
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

        parkingUtility.addParkingLocation(requireContext(), oAuth.getUid(), newLocation);
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

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}