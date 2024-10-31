package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_park, container, false);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
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

        // Add a marker and move the camera
        LatLng location = new LatLng(-34, 151);
        currentMarker = mMap.addMarker(new MarkerOptions().position(location).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        // Add markers for parking spots with custom icon and set click listener
        for (LatLng parkingSpot : parkingSpots) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(parkingSpot)
                    .title("Parking Spot")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));
            marker.setTag("Parking Spot");
            mMap.setOnMarkerClickListener(clickedMarker -> {
                showInfoWindow(clickedMarker, (String) clickedMarker.getTag());
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

                // Add a new marker and move the camera
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // Zoom to the location

                // Add markers for parking spots with custom icon and set click listener
                for (LatLng parkingSpot : parkingSpots) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(parkingSpot)
                            .title("Parking Spot")
                            .icon(bitmapDescriptorFromVector(getContext(), R.drawable.park)));
                    marker.setTag("Parking Spot");
                    mMap.setOnMarkerClickListener(clickedMarker -> {
                        showInfoWindow(clickedMarker, (String) clickedMarker.getTag());
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

    private void showInfoWindow(Marker marker, String title) {
        marker.setTitle(title);
        marker.showInfoWindow();
    }

}
