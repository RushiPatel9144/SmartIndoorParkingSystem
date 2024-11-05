package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingBottomSheetDialog;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingSpotDetails;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;
import ca.tech.sense.it.smart.indoor.parking.system.utility.FavoriteManager;

public class Park extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ParkFragment";

    private GoogleMap mMap;
    private FloatingActionButton fabAddToFavorites;
    private Marker selectedMarker;
    private ParkingUtility parkingUtility;
    private FavoriteManager favoriteManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parkingUtility = new ParkingUtility();
        favoriteManager = new FavoriteManager(requireContext());

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyDv1Ev5porhRyQAUa8s9B96rcLA1OZ6Wzo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park, container, false);
        initializeMap();
        initializeAutocomplete();
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
                    Log.d("tag", "Error: " + status.getStatusMessage());
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable UI controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable traffic, buildings, and indoor maps
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);

        // Map types
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        addParkingSpotsToMap();
        LatLng torontoCenter = new LatLng(43.65107, -79.347015); // Toronto coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(torontoCenter, 10));
        setupMapListeners();
    }

    private void setupMapListeners() {
        mMap.setOnMarkerClickListener(clickedMarker -> {
            selectedMarker = clickedMarker;
            showBookingDialog(clickedMarker);
            return true;
        });
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
                    .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.park)));
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

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }

    private void showBookingDialog(Marker marker) {
        ParkingSpotDetails details = (ParkingSpotDetails) marker.getTag();
        String title = marker.getTitle();
        String message = details != null ? "Address: " + details.getAddress() + "\nPostcode: " + details.getPostcode() : "";

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_booking_details, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        dialogTitle.setText(title);
        dialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        setupDialogButtons(dialog, marker, dialogView);
        dialog.show();
    }

    private void setupDialogButtons(AlertDialog dialog, Marker marker, View dialogView) {
        ImageView ivAddToFavorites = dialogView.findViewById(R.id.iv_add_to_favorites);
        ivAddToFavorites.setOnClickListener(v -> {
            addLocationToFavorites(marker);
            Toast.makeText(getContext(), "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            // Create and show the BookingBottomSheetDialog
            BookingBottomSheetDialog bookingDialog = new BookingBottomSheetDialog(getContext());
            bookingDialog.show(); // Show the bottom sheet dialog

            // Dismiss the current dialog
            dialog.dismiss();
        });

        // Set up the Cancel button
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel); // Assuming you have a button with this ID
        btnCancel.setOnClickListener(v -> {
            // Dismiss the alert dialog
            dialog.dismiss();
        });
    }
}
