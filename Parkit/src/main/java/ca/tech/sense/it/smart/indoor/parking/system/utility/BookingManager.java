package ca.tech.sense.it.smart.indoor.parking.system.utility;

import androidx.annotation.NonNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.model.activity.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

public class BookingManager {

    public BookingManager() {
    }

    public interface FetchLocationCallback {
        void onFetchSuccess(ParkingLocation location);
        void onFetchFailure(Exception exception);
    }

    public void fetchParkingLocation(String locationId, FetchLocationCallback callback) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("parkingLocations").child(locationId);
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ParkingLocation location = snapshot.getValue(ParkingLocation.class);
                if (location != null) {
                    callback.onFetchSuccess(location);
                } else {
                    callback.onFetchFailure(new Exception("Location data is not available"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFetchFailure(error.toException());
            }
        });
    }

    public void fetchPrice(String locationId, Consumer<Double> onSuccess) {
        DatabaseReference priceRef = FirebaseDatabase.getInstance().getReference("parkingLocations").child(locationId).child("price");
        priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double priceValue = snapshot.getValue(Double.class);
                    double price = (priceValue != null) ? priceValue : 0.0;
                    onSuccess.accept(price);
                } else {
                    onSuccess.accept(0.0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onSuccess.accept(0.0);
            }
        });
    }

    public void confirmBooking(String locationId, String slot, String timing, String selectedDate, String address, Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String[] times = timing.split(" - ");
        long startTime = convertToMillis(selectedDate + " " + times[0]);
        long endTime = convertToMillis(selectedDate + " " + times[1]);

        DatabaseReference priceRef = FirebaseDatabase.getInstance().getReference("parkingLocations").child(locationId).child("price");
        priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double priceValue = snapshot.getValue(Double.class);
                    double price = (priceValue != null) ? priceValue : 0.0;

                    Booking booking = new Booking(
                            "Park It", // Use "Park It" as title
                            startTime,
                            endTime,
                            address,
                            slot,
                            price
                    );

                    DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("bookings")
                            .push();

                    databaseRef.setValue(booking)
                            .addOnSuccessListener(aVoid -> onSuccess.run())
                            .addOnFailureListener(onFailure::accept);
                } else {
                    onFailure.accept(new Exception("Price not found for location: " + locationId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onFailure.accept(error.toException());
            }
        });
    }

    public void saveLocationToFavorites(String locationId, String address, Runnable onSuccess, Consumer<Exception> onFailure) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("locationId", locationId);
        locationData.put("address", address);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("saved_locations").child(locationId);

        databaseRef.setValue(locationData).addOnSuccessListener(aVoid -> onSuccess.run()).addOnFailureListener(onFailure::accept);
    }

    private long convertToMillis(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

