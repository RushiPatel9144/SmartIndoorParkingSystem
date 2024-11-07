/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ca.tech.sense.it.smart.indoor.parking.system.R;

import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingBottomSheetDialog;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;

public class Park extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ParkFragment";
    private GoogleMap mMap;
    private ParkingUtility parkingUtility;
    private ExecutorService executorService;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parkingUtility = new ParkingUtility();
        executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

        // Register the ActivityResultLauncher for permission handling
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        enableMyLocation();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_park, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        executorService.execute(() -> {
            requireActivity().runOnUiThread(() -> {
                if (!Places.isInitialized()) {
                    Places.initialize(requireContext(), "AIzaSyCBb9Vk3FUhAz6Tf7ixMIk5xqu3IGlZRd0"); // Initialize Places API only once
                }
                initializeMap();
                initializeAutocomplete();
            });
        });
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
            autocompleteFragment.setHint(getString(R.string.search_for_a_location));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Handle place selection in a background thread
                    executorService.execute(() -> handlePlaceSelected(place));
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.d(TAG, getString(R.string.error)+ status.getStatusMessage());
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        setupMapUI();
        moveMyLocationButton();
        checkLocationPermissionAndEnableMyLocation();

        // Asynchronous task to add parking spots
        executorService.execute(this::addParkingSpotsToMap);

        // Default camera position
        LatLng torontoCenter = new LatLng(43.65107, -79.347015); // Toronto coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(torontoCenter, 10));
        setupMapListeners();
    }

    private void setupMapUI() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void handlePlaceSelected(Place place) {
        if (place.getLatLng() != null) {
            requireActivity().runOnUiThread(() -> {
                mMap.clear(); // Clear any existing markers
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                addParkingSpotsToMap(); // Add parking spots after place selection
            });
        }
    }

    private void addParkingSpotsToMap() {
        // Fetch parking locations in the background
        parkingUtility.fetchAllParkingLocations(new ParkingUtility.FetchLocationsCallback() {
            @Override
            public void onFetchSuccess(Map<String, ParkingLocation> locations) {
                requireActivity().runOnUiThread(() -> {
                    if (locations != null) {
                        for (Map.Entry<String, ParkingLocation> entry : locations.entrySet()) {
                            ParkingLocation location = entry.getValue();
                            if (isValidParkingLocation(location)) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(location.getName())
                                        .icon(bitmapDescriptorFromVector(requireContext(), R.mipmap.ic_parking))
                                );
                                marker.setTag(location.getId());
                            }
                        }
                    }
                });
            }

            @Override
            public void onFetchFailure(Exception e) {
                Log.e(TAG, getString(R.string.error_fetching_parking_locations), e);
                Toast.makeText(requireContext(), getString(R.string.failed_to_load_parking_locations), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidParkingLocation(ParkingLocation location) {
        return location.getLatitude() >= -90 && location.getLatitude() <= 90 &&
                location.getLongitude() >= -180 && location.getLongitude() <= 180 &&
                location.getName() != null;
    }

    private void setupMapListeners() {
        mMap.setOnMarkerClickListener(clickedMarker -> {
            showBookingBottomSheet(clickedMarker);
            return true;
        });
    }

    private void showBookingBottomSheet(Marker marker) {
        String parkingLocationId = (String) marker.getTag();
        BookingBottomSheetDialog bookingDialog = new BookingBottomSheetDialog(requireContext(), parkingLocationId);
        bookingDialog.show();
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

    private void checkLocationPermissionAndEnableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void moveMyLocationButton() {
        if (getView() != null) {
            View locationButton = getView().findViewById(Integer.parseInt(getString(R.string._1)));
            if (locationButton != null && locationButton.getParent() != null) {
                View parent = (View) locationButton.getParent();
                View myLocationButton = parent.findViewById(Integer.parseInt(getString(R.string._2)));
                if (myLocationButton != null) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();
                    // Adjust these values to set the desired position
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0); // Remove top alignment
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE); // Align to the bottom
                    layoutParams.setMargins(0, 0, 30, 350); // Adjust margins as needed
                    myLocationButton.setLayoutParams(layoutParams);
                }
            }
        }
    }
}
