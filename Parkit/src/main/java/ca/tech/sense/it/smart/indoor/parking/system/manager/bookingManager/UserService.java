package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Transaction;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DateTimeUtils;

public class UserService {

    private final ExecutorService executorService;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;
    private TransactionManager transactionManager;
    private static final String COLLECTION = "users";
    private static final String PATH = "bookings";


    public UserService(ExecutorService executorService, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth) {
        this.executorService = executorService;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;

    }

    public void saveLocationToFavorites(String locationId, String address, String postalCode, String name, Runnable onSuccess, Consumer<Exception> onFailure) {
        executorService.submit(() -> {
            String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("locationId", locationId);
            locationData.put("address", address);
            locationData.put("postalCode", postalCode);
            locationData.put("name", name); // Add name to the data

            DatabaseReference databaseRef = firebaseDatabase.getReference(COLLECTION).child(userId).child("saved_locations").child(locationId);

            databaseRef.setValue(locationData).addOnSuccessListener(aVoid -> onSuccess.run()).addOnFailureListener(onFailure::accept);
        });
    }

    public void clearAllBookingHistory(String userId, Consumer<List<Booking>> onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference bookingsRef = firebaseDatabase.getReference(COLLECTION).child(userId).child(PATH);
        bookingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> bookings = new ArrayList<>();
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }
                onSuccess.accept(bookings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onFailure.accept(new Exception(error.getMessage()));
            }
        });
    }


    public void expirePassKey(String userId, String bookingId) {
        DatabaseReference bookingRef = firebaseDatabase.getReference(COLLECTION)
                .child(userId)
                .child(PATH)
                .child(bookingId)
                .child("passKey");

        bookingRef.setValue(null) // Remove the pass key
                .addOnSuccessListener(aVoid -> { // Pass key expired successfully
                    })
                .addOnFailureListener(e ->
                    // Handle the error
                    Toast.makeText(null, "Failed to expire pass key: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void processOwnerData(String locationId, Booking booking, String refundId) {
        if (transactionManager == null) {
            transactionManager = new TransactionManager(firebaseDatabase);
        }
       transactionManager.fetchOwnerIdByLocationId(locationId)
                .addOnSuccessListener(ownerId -> transactionUpdate(ownerId, booking, refundId))
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }

    private void transactionUpdate(String ownerId, Booking booking, String refundId) {
        if (transactionManager == null) {
            transactionManager = new TransactionManager(firebaseDatabase);
        }
        transactionManager.storeTransaction(ownerId, new Transaction(
                refundId,
                booking.getTitle(),
                booking.getPrice(),
                booking.getCurrencySymbol(),
                DateTimeUtils.getCurrentDateTime(),
                true
        ));
    }

    public void cancelBookingAndRequestRefund(String userId, Booking booking, Runnable onSuccess, Consumer<Exception> onFailure) {
        transactionManager = new TransactionManager(firebaseDatabase);
       cancelBooking(userId, booking.getId(), () -> requestRefund(booking, refundId -> {
           processOwnerData(booking.getLocationId(), booking, refundId);
            onSuccess.run();
        }, onFailure), onFailure);
    }


    public void cancelBooking(String userId, String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        if (bookingId == null) {
            onFailure.accept(new Exception("Booking ID is null"));
            return;
        }

        DatabaseReference bookingRef = firebaseDatabase.getReference(COLLECTION).child(userId).child(PATH).child(bookingId);
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    // Remove the booking
                    bookingRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Update the slot status to "available"
                                SlotService slotService = new SlotService(executorService, firebaseDatabase, Executors.newScheduledThreadPool(1));
                                slotService.updateSlotStatusToAvailable(booking.getLocationId(), booking.getSlotNumber(), booking.getStartTime(), booking.getEndTime(), onSuccess, onFailure);
                            })
                            .addOnFailureListener(onFailure::accept);
                } else {
                    onFailure.accept(new Exception("Booking not found"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onFailure.accept(error.toException());
            }
        });
    }

    private void requestRefund(Booking booking, Consumer<String> onSuccess, Consumer<Exception> onFailure) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://parkit-cd4c2ec26f90.herokuapp.com/refund-payment";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("transactionId", booking.getTransactionId());
            jsonRequest.put("amount", booking.getPrice());
        } catch (JSONException e) {
            onFailure.accept(e);
            return;
        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), jsonRequest.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> onFailure.accept(new Exception("Failed to connect to server for refund")));
            }
            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);
                        String refundId = responseJson.optString("refundId");
                        if (!refundId.isEmpty()) {
                            final String finalRefundId = refundId;
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .post(() -> onSuccess.accept(finalRefundId));
                        } else {
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .post(() -> onFailure.accept(new Exception("Refund ID not found in response")));
                        }
                    } catch (Exception e) {
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .post(() -> onFailure.accept(new Exception("Error processing response: " + e.getMessage())));
                    }
                } else {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .post(() -> onFailure.accept(new Exception("Server error: " + response.message())));
                }
            }
        });
    }

    public void clearBookingHistory(String userId, String bookingId, Runnable onSuccess, Consumer<Exception> onFailure) {
        if (bookingId == null) {
            onFailure.accept(new Exception("Booking ID is null"));
            return;
        }
        DatabaseReference bookingRef = firebaseDatabase.getReference(COLLECTION).child(userId).child(PATH).child(bookingId);
        bookingRef.removeValue()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }
}

