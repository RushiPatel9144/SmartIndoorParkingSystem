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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class Park extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchBar;
    private Button searchButton;
    private Marker currentMarker;
    private List<LatLng> parkingSpots;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize parking spots
        parkingSpots = new ArrayList<>();
        parkingSpots.add(new LatLng(43.7289, -79.6077)); // Example coordinates for Humber College
        parkingSpots.add(new LatLng(43.73009, -79.5987)); // SP+ Parking
        parkingSpots.add(new LatLng(43.731636, -79.61172)); // Green P Parking
        parkingSpots.add(new LatLng(43.690456, -79.60008)); // Park For U YYZ Airport Parking
        // Add more parking spots as needed
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_park, container, false);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the search bar and button
        searchBar = view.findViewById(R.id.search_bar);
        searchButton = view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            String location = searchBar.getText().toString();
            if (!location.isEmpty()) {
                searchLocation(location);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add markers for parking spots with custom icon and set click listener
        for (LatLng parkingSpot : parkingSpots) {
          
            Marker marker = mMap.addMarker(new MarkerOptions().position(parkingSpot).title("Parking Spot").icon(bitmapDescriptorFromVector(getContext(), R.drawable.parking)));

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(parkingSpot)
                    .title("Parking Spot")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));

            marker.setTag("Parking Spot");
            mMap.setOnMarkerClickListener(clickedMarker -> {
                showBookingDialog(clickedMarker);
                return true;
            });
        }
    }

    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Remove the previous marker
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                // Clear existing markers
                mMap.clear();

                // Add a new marker for the searched location
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // Zoom to the location

                // Re-add markers for parking spots with custom icon and set click listener
                for (LatLng parkingSpot : parkingSpots) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(parkingSpot)
                            .title("Parking Spot")

                            .icon(bitmapDescriptorFromVector(getContext(), R.drawable.parking)));
                    marker.setTag(new ParkingSpotDetails("123 Example St, Toronto, ON", "M1A 2B3", R.drawable.park));

                    mMap.setOnMarkerClickListener(clickedMarker -> {
                        showBookingDialog(clickedMarker);
                        return true;
                    });
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
        // Inflate the booking dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.booking_dialog, null);
        TextView tvParkingLocation = dialogView.findViewById(R.id.tv_parking_location);
        TextView tvParkingAddress = dialogView.findViewById(R.id.tv_parking_address);
        TextView tvParkingPostcode = dialogView.findViewById(R.id.tv_parking_postcode);
        ImageView ivParkingImage = dialogView.findViewById(R.id.iv_parking_image);
        Button btnConfirmBooking = dialogView.findViewById(R.id.btn_confirm_booking);

        // Retrieve the parking spot details from the marker's tag
        ParkingSpotDetails details = (ParkingSpotDetails) marker.getTag();

        // Set the parking location in the TextView
        tvParkingLocation.setText(marker.getTitle());

        // Set additional details
        if (details != null) {
            tvParkingAddress.setText(details.getAddress());
            tvParkingPostcode.setText(details.getPostcode());
            ivParkingImage.setImageResource(details.getImageResId());
        }

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle booking confirmation
        btnConfirmBooking.setOnClickListener(v -> {
            // Handle booking logic (e.g., save to database, send confirmation email)
            Toast.makeText(getContext(), "Booking confirmed for " + marker.getTitle(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }


}
