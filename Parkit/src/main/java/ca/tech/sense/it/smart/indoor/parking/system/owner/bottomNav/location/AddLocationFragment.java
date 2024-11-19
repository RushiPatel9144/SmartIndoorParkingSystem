package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class AddLocationFragment extends Fragment {

    private EditText locationName;
    private EditText postalCode;
    private EditText price;
    private AutoCompleteTextView addressSearchBar;
    private Button confirmButton;
    private Button cancelButton;

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
        initializeUI(view);
        return view;
    }
    private void initializeUI(View rootView) {
        // Initialize Views
        locationName = rootView.findViewById(R.id.locationName);
        addressSearchBar = rootView.findViewById(R.id.addressSearchBar);;
        postalCode = rootView.findViewById(R.id.postal_code);
        price = rootView.findViewById(R.id.price);
        confirmButton = rootView.findViewById(R.id.confirmButton);
        cancelButton = rootView.findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(v -> onConfirmButtonClicked());
        cancelButton.setOnClickListener(v -> onCancelButtonClicked());
    }

    private void onConfirmButtonClicked() {
        String locationNameStr = locationName.getText().toString();
        String addressStr = addressSearchBar.getText().toString();
        String postalCodeStr = postalCode.getText().toString();
        String priceStr = price.getText().toString();

        if (locationNameStr.isEmpty() || addressStr.isEmpty() || postalCodeStr.isEmpty() || priceStr.isEmpty()) {
            showToast("Please fill all fields");
        } else {
            showToast("Parking Location Added Successfully");
        }
    }

    private void onCancelButtonClicked() {

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}