package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

public class PaymentActivity extends AppCompatActivity {

    private Booking booking;
    private BookingManager bookingManager;
    private TextView parkingNameTextView;
    private TextView addressTextView;
    private TextView postalCodeTextView;
    private TextView subtotalTextView;
    private TextView gstHstTextView;
    private TextView platformFeeTextView;
    private TextView totalTextView;
    private TextView slotTextView;
    private TextView timeTextView;
    private TextView dateTextView;
    private EditText promoCodeEditText;
    private Button applyPromoCodeButton;
    private Button confirmButton;
    private Button cancelButton;
    private PaymentSheet paymentSheet;
    private String currency = "CAD";
    private double total;
    private String transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment2); // Update to your layout
        initializeUIElements();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("booking")) {
            booking = (Booking) intent.getSerializableExtra("booking");
            if (booking != null) {
                setBookingDetails();
                calculateTotalBreakdown();
            } else {
                showToast(getString(R.string.booking_data_is_missing_or_invalid));
            }
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabaseSingleton.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuthSingleton.getInstance();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Context context = this;

        bookingManager = new BookingManager(executorService, firebaseDatabase, firebaseAuth, context);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51QJqGPAwqnUq4xSjJQmV0FKH27StbQRdn5jfOtsyFy3tJadwpId67LbynKlh3aonDKstIxv59LWbhFGGlwTTOTJi00QX02RzJp"
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        setButtonListeners();
        setupPromoCodeEditText();
    }

    private void initializeUIElements() {
        parkingNameTextView = findViewById(R.id.parkingNameTextView);
        addressTextView = findViewById(R.id.addressTextView);
        postalCodeTextView = findViewById(R.id.postalCodeTextView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        gstHstTextView = findViewById(R.id.gstHstTextView);
        platformFeeTextView = findViewById(R.id.platformFeeTextView);
        totalTextView = findViewById(R.id.totalTextView);
        promoCodeEditText = findViewById(R.id.promoCodeEditText);
        applyPromoCodeButton = findViewById(R.id.applyPromoCodeButton);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);
        slotTextView = findViewById(R.id.slotTextView);
        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
    }

    private void setBookingDetails() {
        if (booking != null) {
            parkingNameTextView.setText(booking.getTitle());
            addressTextView.setText(booking.getLocation());
            postalCodeTextView.setText(booking.getPostalCode());
            slotTextView.setText(String.format("Slot Number: %s", booking.getSlotNumber()));
            updateDateAndTimeTextViews(booking.getStartTime(), booking.getEndTime());
        }
    }

    private void calculateTotalBreakdown() {
        if (booking != null) {
            String currencySymbol = booking.getCurrencySymbol();
            double subtotal = booking.getPrice();
            double gstHst = subtotal * 0.13;
            double platformFee = subtotal * 0.10;
            total = subtotal + gstHst + platformFee;

            subtotalTextView.setText(String.format(Locale.getDefault(), "%s %.2f",currencySymbol, subtotal));
            gstHstTextView.setText(String.format(Locale.getDefault(), "%s %.2f",currencySymbol, gstHst));
            platformFeeTextView.setText(String.format(Locale.getDefault(), "%s %.2f",currencySymbol, platformFee));
            totalTextView.setText(String.format(Locale.getDefault(), "%s %.2f",currencySymbol, total));
        }
    }

    private void setButtonListeners() {
        applyPromoCodeButton.setOnClickListener(v -> {
            String promoCode = promoCodeEditText.getText().toString().trim();
            if (!promoCode.isEmpty()) {
                applyPromoCode(promoCode);
            } else {
                showToast("Please enter a promo code.");
            }
        });
        confirmButton.setOnClickListener(v -> fetchClientSecret());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void fetchClientSecret() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://parkit-cd4c2ec26f90.herokuapp.com/create-payment-intent";

        double totalAmount = total * 100;
        currency = booking.getCurrencyCode();

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("amount", (int) totalAmount);
            jsonRequest.put("currency", currency);
        } catch (JSONException e) {
            e.printStackTrace();
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
                runOnUiThread(() -> showToast(getString(R.string.failed_to_connect_to_server)));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String clientSecret = jsonResponse.getString("clientSecret");
                        transactionId = jsonResponse.getString("transactionId");
                        runOnUiThread(() -> startPaymentFlow(clientSecret));
                    } catch (JSONException e) {
                        runOnUiThread(() -> showToast(getString(R.string.failed_to_parse_server_response)));
                    }
                } else {
                    runOnUiThread(() -> showToast("Server error: " + response.message()));
                }
            }
        });
    }

    private void startPaymentFlow(String clientSecret) {
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("ParkIt").build();
        paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Use Handler with Looper.getMainLooper() for a delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                confirmBooking(); // Confirm the booking
                openParkingTicketActivity(); // Pass data to ParkingTicket
            }, 2000); // 2-second delay
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            showToast("Payment Failed: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            showToast("Payment Canceled");
            finish();
        }
    }

    private void confirmBooking() {
        if (booking == null) {
            showToast(getString(R.string.booking_data_is_missing));
            return;
        }

        // Extract valid time slot and date
        String selectedTimeSlot = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(booking.getStartTime())) + " - " +
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(booking.getEndTime()));
        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(booking.getStartTime()));

        try {
            bookingManager.getBookingService().confirmBooking(
                    transactionId,
                    booking.getLocationId(),        // Pass location ID
                    booking.getSlotNumber(),       // Pass slot number
                    selectedTimeSlot,              // Valid time slot
                    selectedDate,                  // Valid date
                    booking.getLocation(),         // Location
                    () -> {
                        showToast(getString(R.string.booking_confirmed));

                        // Validate and mark the promo code as used after booking confirmation
                        String promoCode = promoCodeEditText.getText().toString().trim();
                        if (!promoCode.isEmpty()) {
                            DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference("Promotions");
                            promotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Promotion promotion = snapshot.getValue(Promotion.class);
                                        if (promotion != null && promoCode.equals(promotion.getPromoCode())) {
                                            promotion.setUsed(true); // Mark as used only after booking
                                            promotionsRef.child(promotion.getId()).setValue(promotion);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    showToast("Failed to update promo code status.");
                                }
                            });
                        }
                    },
                    error -> showToast("Failed to save booking: " + error.getMessage()));
        } catch (Exception e) {
            // Catch unexpected exceptions
            showToast("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void updateDateAndTimeTextViews(long startTimeMillis, long endTimeMillis) {
        // Format date (without time)
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(startTimeMillis));

        // Format time (without date)
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedStartTime = timeFormat.format(new Date(startTimeMillis));
        String formattedEndTime = timeFormat.format(new Date(endTimeMillis));

        // Update the TextViews with the formatted date and time
        dateTextView.setText(MessageFormat.format("Date: {0}", formattedDate));
        timeTextView.setText(MessageFormat.format("Time: {0} - {1}", formattedStartTime, formattedEndTime));
    }



    private void openParkingTicketActivity() {
        Intent intent = new Intent(this, ParkingTicket.class);

        if (booking != null) {
            intent.putExtra("address", booking.getLocation()); // Pass address
            intent.putExtra("passkey", booking.getPassKey());  // Pass reference key
        }

        startActivity(intent);
        finish();
    }

    private void applyPromoCode(String promoCode) {
        DatabaseReference promotionsRef = FirebaseDatabase.getInstance().getReference("Promotions");
        promotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isValidPromo = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    if (promotion != null && promoCode.equals(promotion.getPromoCode()) && !promotion.isUsed()) {
                        isValidPromo = true;
                        double discount = promotion.getDiscount();
                        double subtotal = booking.getPrice();
                        double discountAmount = subtotal * (discount / 100);
                        double newSubtotal = subtotal - discountAmount;
                        double gstHst = newSubtotal * 0.13;
                        double platformFee = newSubtotal * 0.10;
                        total = newSubtotal + gstHst + platformFee;

                        subtotalTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), newSubtotal));
                        gstHstTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), gstHst));
                        platformFeeTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), platformFee));
                        totalTextView.setText(String.format(Locale.getDefault(), "%s %.2f", booking.getCurrencySymbol(), total));

                        showToast("Promo code applied successfully!");
                        break;
                    }
                }
                if (!isValidPromo) {
                    showToast("Invalid or already used promo code.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to validate promo code.");
            }
        });
    }

    private void setupPromoCodeEditText() {
        promoCodeEditText.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence pastedText = clip.getItemAt(0).getText();
                    promoCodeEditText.setText(pastedText);
                    promoCodeEditText.setSelection(promoCodeEditText.getText().length()); // Move cursor to the end
                    showToast("Promo code copied. It will be applied at the time of payment.");
                }
            }
        });
    }
}
