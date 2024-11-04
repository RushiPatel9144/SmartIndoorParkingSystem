package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingSpotDetails;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;
import ca.tech.sense.it.smart.indoor.parking.system.utility.FavoriteManager;

public class Park extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingActionButton fabAddToFavorites;
    private Marker selectedMarker;
    private ParkingUtility parkingUtility;
    private FavoriteManager favoriteManager;
    private BookingManager bookingManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parkingUtility = new ParkingUtility();
        favoriteManager = new FavoriteManager(getContext());
        bookingManager = BookingManager.getInstance(); // Initialize BookingManager

        // Initialize the Places SDK with your API key
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBGsYK3svittnwcoP6dF7WiOow0T4mWedo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park, container, false);

        initializeMap();
        initializeAutocomplete();
        initializeFab(view);

        return view;
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("Search for a location");
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    handlePlaceSelected(place);
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        addParkingSpotsToMap();
        // Set the camera to Toronto (coordinates: latitude 43.65107, longitude -79.347015)
        LatLng torontoCenter = new LatLng(43.65107, -79.347015); // Coordinates for Toronto
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(torontoCenter, 10)); // Zoom level 10 for a good view of the city
        mMap.setOnMarkerClickListener(clickedMarker -> {
            selectedMarker = clickedMarker;
            showBookingDialog(clickedMarker);
            fabAddToFavorites.setVisibility(View.VISIBLE);
            return true;
        });

        mMap.setOnMapClickListener(latLng -> fabAddToFavorites.setVisibility(View.GONE));
    }

    private void handlePlaceSelected(Place place) {
        if (place.getLatLng() != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            addParkingSpotsToMap();
        }
    }

    private void addParkingSpotsToMap() {
        List<LatLng> parkingSpots = parkingUtility.getParkingSpots();
        for (LatLng parkingSpot : parkingSpots) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(parkingSpot)
                    .title("Parking Spot")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));
            marker.setTag(parkingUtility.getSpotDetails(parkingSpot));
        }
    }

    private void addLocationToFavorites(Marker marker) {
        if (marker != null) {
            LatLng location = marker.getPosition();
            favoriteManager.addFavorite(location);
            Toast.makeText(getContext(), "Location added to favorites!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeFab(View view) {
        fabAddToFavorites = view.findViewById(R.id.fab_add_to_favorites);
        fabAddToFavorites.setOnClickListener(v -> addLocationToFavorites(selectedMarker));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void showBookingDialog(Marker marker) {
        ParkingSpotDetails details = (ParkingSpotDetails) marker.getTag();
        String title = marker.getTitle();
        String message = details != null ? "Address: " + details.getAddress() + "\nPostcode: " + details.getPostcode() : "";

        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_booking_details, null);

        // Set the title and message
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        dialogTitle.setText(title);
        dialogMessage.setText(message);

        // Create the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        // Handle the favorite icon click
        ImageView ivAddToFavorites = dialogView.findViewById(R.id.iv_add_to_favorites);
        ivAddToFavorites.setOnClickListener(v -> {
            addLocationToFavorites(marker); // Call your method to add to favorites
            Toast.makeText(getContext(), "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });

        // Set dialog button actions
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            // Navigate to BookingDetailsFragment
            BookingDetailsFragment bookingDetailsFragment = new BookingDetailsFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.flFragment, bookingDetailsFragment) // Replace with your container ID
                    .addToBackStack(null)
                    .commit();
            dialog.dismiss();
        });

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



}
