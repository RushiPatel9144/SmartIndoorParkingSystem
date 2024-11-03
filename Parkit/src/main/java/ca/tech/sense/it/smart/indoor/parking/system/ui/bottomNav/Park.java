package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingUtility;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingSpotDetails;

public class Park extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentMarker;
    private ParkingUtility parkingUtility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parkingUtility = new ParkingUtility();
        // Initialize the Places SDK with your API key
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), "AIzaSyBGsYK3svittnwcoP6dF7WiOow0T4mWedo");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park, container, false);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint("Search for a location");

        // Add a listener to handle the result from the autocomplete widget
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Get the selected place's location
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    mMap.clear();

                    // Add a marker to the selected location
                    mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    // Re-add parking spot markers
                    List<LatLng> parkingSpots = parkingUtility.getParkingSpots();
                    for (LatLng parkingSpot : parkingSpots) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(parkingSpot)
                                .title("Parking Spot")
                                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));
                        marker.setTag(parkingUtility.getSpotDetails(parkingSpot));
                    }
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle error
                Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add markers for parking spots
        List<LatLng> parkingSpots = parkingUtility.getParkingSpots();
        for (LatLng parkingSpot : parkingSpots) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(parkingSpot)
                    .title("Parking Spot")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));
            marker.setTag(parkingUtility.getSpotDetails(parkingSpot));
        }

        mMap.setOnMarkerClickListener(clickedMarker -> {
            showBookingDialog(clickedMarker);
            return true;
        });
    }

    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Remove the previous marker
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                // Clear existing markers and add new markers for parking spots
                mMap.clear();
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                List<LatLng> parkingSpots = parkingUtility.getParkingSpots();
                for (LatLng parkingSpot : parkingSpots) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(parkingSpot)
                            .title("Parking Spot")
                            .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));
                    marker.setTag(parkingUtility.getSpotDetails(parkingSpot));
                }

            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error finding location", Toast.LENGTH_SHORT).show();
        }
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.booking_dialog, null);
        TextView tvParkingLocation = dialogView.findViewById(R.id.tv_parking_location);
        TextView tvParkingAddress = dialogView.findViewById(R.id.tv_parking_address);
        TextView tvParkingPostcode = dialogView.findViewById(R.id.tv_parking_postcode);
        ImageView ivParkingImage = dialogView.findViewById(R.id.iv_parking_image);
        Button btnConfirmBooking = dialogView.findViewById(R.id.btn_confirm_booking);

        ParkingSpotDetails details = (ParkingSpotDetails) marker.getTag();
        tvParkingLocation.setText(marker.getTitle());

        if (details != null) {
            tvParkingAddress.setText(details.getAddress());
            tvParkingPostcode.setText(details.getPostcode());
            ivParkingImage.setImageResource(details.getImageResId());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnConfirmBooking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Booking confirmed for " + marker.getTitle(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }
}
