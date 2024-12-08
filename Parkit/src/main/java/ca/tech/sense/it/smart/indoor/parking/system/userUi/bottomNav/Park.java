/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingBottomSheetDialogFragment;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyService;
import ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AutocompleteSearchHelper;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingInterface;

public class Park extends BaseNetworkFragment implements OnMapReadyCallback {

    private static final String TAG = "ParkFragment";
    private GoogleMap mMap;
    private final ParkingLocationManager parkingLocationManager = new ParkingLocationManager();
    private ExecutorService executorService;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean ratesFetched = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newFixedThreadPool(4);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (!ratesFetched) {
            fetchExchangeRates();
        }
        registerPermissionLauncher();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park, container, false);
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(),"AIzaSyCBb9Vk3FUhAz6Tf7ixMIk5xqu3IGlZRd0");
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        executorService.execute(() -> requireActivity().runOnUiThread(() -> {
            initializeMap();
            initializeAutocomplete();
        }));

        // Check if there is a locationId passed from the FavoritesFragment
        if (getActivity() != null && getActivity().getIntent() != null) {
            String locationId = getActivity().getIntent().getStringExtra("locationId");
            if (locationId != null) {
                showBookingBottomSheet(locationId);
            }
        }
    }


    private void initializeMap() {
        if (getView() != null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
    }

    private void initializeAutocomplete() {
        if (getView() != null) {
            AutocompleteSearchHelper.initializeAutocompleteSearch(
                    (AutocompleteSupportFragment) Objects.requireNonNull(getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment)),
                    requireContext(),
                    new AutocompleteSearchHelper.PlaceSelectionCallback() {
                        @Override
                        public void onPlaceSelected(Place place) {
                            executorService.execute(() -> handlePlaceSelected(place));
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d(TAG, getString(R.string.error) + errorMessage);
                        }
                    }
            );
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        setupMapUI();
        ImageButton myLocationButton = requireView().findViewById(R.id.my_location_button);
        myLocationButton.setOnClickListener(v -> checkLocationPermissionAndEnableMyLocation());
        // Asynchronous task to add parking spots
        executorService.execute(this::addParkingSpotsToMap);

        // Default camera position
        LatLng torontoCenter = new LatLng(43.65107, -79.347015); // Toronto coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(torontoCenter, 10));
        setupMapListeners();
    }

    private void setupMapUI() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        mMap.setTrafficEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void handlePlaceSelected(Place place) {
        if (place.getLocation() != null) {
            requireActivity().runOnUiThread(() -> {
                mMap.clear();
                new Handler(Looper.getMainLooper()).postDelayed(() -> mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLocation(), 15)), 500);
                addParkingSpotsToMap(); // Add parking spots after place selection
            });
        }
    }

    private void addParkingSpotsToMap() {
        // Fetch parking locations in the background
        parkingLocationManager.fetchAllParkingLocations(new ParkingInterface.FetchLocationsCallback() {
            @Override
            public void onFetchSuccess(Map<String, ParkingLocation> locations) {
                if (!isAdded()) {
                    return;
                }
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
                                assert marker != null;
                                marker.setTag(location.getId());
                            }
                        }
                    }
                });
            }

            @Override
            public void onFetchFailure(Exception e) {
                if (!isAdded()) {
                    return;
                }
                Log.e(TAG, getString(R.string.error_fetching_parking_locations), e);
                Toast.makeText(requireContext(), Toast.LENGTH_SHORT, R.string.failed_to_load_parking_locations).show();
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

    public void showBookingBottomSheet(Marker marker) {
        String parkingLocationId = (String) marker.getTag();
        // Create an instance of ExecutorService

        // Get instances of FirebaseDatabase and FirebaseAuth
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        BookingManager bookingManager = new BookingManager(executorService, firebaseDatabase, firebaseAuth,getContext());
        BookingBottomSheetDialogFragment paymentFragment = new BookingBottomSheetDialogFragment (executorService,parkingLocationId, bookingManager, getContext());
        paymentFragment.show(getChildFragmentManager(), "BookingBottomSheetDialogFragment");
    }

    public void showBookingBottomSheet(String locationId) {
        // Create an instance of ExecutorService
        // Get instances of FirebaseDatabase and FirebaseAuth
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        BookingManager bookingManager = new BookingManager(executorService, firebaseDatabase, firebaseAuth, getContext());
        BookingBottomSheetDialogFragment bookingDialog = new BookingBottomSheetDialogFragment(executorService, locationId, bookingManager, getContext());

        bookingDialog.show(getChildFragmentManager(), "BookingBottomSheetDialogFragment");
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
            // Get the last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15)); // Zoom level 15 is just an example
                            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
                        } else {
                            Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void registerPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (Boolean.TRUE.equals(isGranted)) {
                        enableMyLocation();
                    } else {
                        Toast.makeText(getContext(), R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void fetchExchangeRates() {
        CurrencyManager.getInstance().fetchAndUpdateRates(requireContext(), new CurrencyService.Callback() {
            @Override
            public void onSuccess(Map<String, Double> exchangeRates) {
                Log.d("Currency", "Exchange rates fetched successfully.");
                ratesFetched = true;
            }
            @Override
            public void onError(String error) {
                Log.e("Currency", "Error fetching exchange rates: " + error);
                ratesFetched = true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
