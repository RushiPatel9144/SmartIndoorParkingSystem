package ca.tech.sense.it.smart.indoor.parking.system.utility;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ParkingUtility {

    // Method to fetch parking spots as LatLng objects
    public List<LatLng> getParkingSpots() {
        List<LatLng> parkingSpots = new ArrayList<>();
        parkingSpots.add(new LatLng(43.7289, -79.6077)); // Example for Humber College
        parkingSpots.add(new LatLng(43.73009, -79.5987)); // Example for SP+ Parking
        parkingSpots.add(new LatLng(43.731636, -79.61172)); // Example for Green P Parking
        parkingSpots.add(new LatLng(43.690456, -79.60008)); // Example for Park For U YYZ Airport Parking
        return parkingSpots;
    }

    // Method to get details for a parking spot, e.g., address and image resource
    public ParkingSpotDetails getSpotDetails(LatLng location) {
        // Example data for simplicity; you may fetch real data from a database or API
        return new ParkingSpotDetails("123 Example St, Toronto, ON", "M1A 2B3", R.drawable.park);
    }

    // Method to get sensor data (if needed)
    public String getSensorData() {
        // Placeholder for sensor data logic
        return "Sensor data not available";
    }
}
