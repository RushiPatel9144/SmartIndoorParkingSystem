package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.TransactionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.Promotion;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.utility.PromotionHelper;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Transaction;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DateTimeUtils;


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
    private double total;
    private String transactionId;
    private TransactionManager transactionManager ;
    private String ownerId;
    private double subtotal;
    FirebaseAuth firebaseAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment2); // Update to your layout
        initializeUIElements();

        Intent intent = getIntent();
        ownerId = intent.getStringExtra("ownerId"); // Safely fetch ownerId
        if (intent != null) {
            if (intent.hasExtra("booking")) {
                booking = (Booking) intent.getSerializableExtra("booking");
                if (booking != null) {
                    setBookingDetails();
                    calculateTotalBreakdown();
                } else {
                    showToast(getString(R.string.booking_data_is_missing_or_invalid));
                    // Optionally finish the activity if booking data is critical
                    finish();
                }
            } else {
                showToast(getString(R.string.booking_data_is_missing_or_invalid));
                // Optionally finish the activity if booking data is critical
                finish();
            }
        } else {
            showToast(getString(R.string.intent_data_is_missing));
            // Optionally finish the activity if the intent is null
            finish();
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabaseSingleton.getInstance();
        firebaseAuth = FirebaseAuthSingleton.getInstance();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Context context = this;
        transactionManager = new TransactionManager(firebaseDatabase);
        bookingManager = new BookingManager(executorService, firebaseDatabase, firebaseAuth, context);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51QJqGPAwqnUq4xSjJQmV0FKH27StbQRdn5jfOtsyFy3tJadwpId67LbynKlh3aonDKstIxv59LWbhFGGlwTTOTJi00QX02RzJp"
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        setButtonListeners();
        PromotionHelper.setupPromoCodeEditText(promoCodeEditText, this);
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
            subtotal = booking.getPrice();
            double gstHst = subtotal * 0.13;
            double platformFee = subtotal * 0.10;
            total = subtotal + gstHst + platformFee;

            booking.setTotalPrice(CurrencyManager.getInstance().convertToCAD(total, booking.getCurrencyCode()));
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
                PromotionHelper.applyPromoCode(promoCode, booking, subtotalTextView, gstHstTextView, platformFeeTextView, totalTextView, this);
            } else {
                showToast("Please enter a promo code.");
            }
        });
        confirmButton.setOnClickListener(v -> fetchClientSecret(total, booking));
        cancelButton.setOnClickListener(v -> finish());
    }

    private void fetchClientSecret(double price, Booking booking) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://parkit-cd4c2ec26f90.herokuapp.com/create-payment-intent";
        double totalAmount = price * 100;
        String currency = booking.getCurrencyCode();

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
                markPromoCodeAsUsed(); // Mark the promo code as used
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
        String selectedTimeSlot = formatTimeSlot(booking.getStartTime(), booking.getEndTime());
        String selectedDate = formatDate(booking.getStartTime());

        // Create a transaction object
        Transaction transaction = createTransaction();

        // Store transaction and confirm booking
        try {
            transactionManager.storeTransaction(ownerId, transaction);
            confirmBookingInService(transactionId, selectedTimeSlot, selectedDate);
        } catch (Exception e) {
            showToast("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void markPromoCodeAsUsed() {
        String promoCode = promoCodeEditText.getText().toString().trim();
        if (!promoCode.isEmpty()) {
            PromotionHelper.markPromoCodeAsUsed(promoCode, this);
        }
    }


    private void onBookingFailure(Exception e) {
        showToast("Booking failed: " + e.getMessage());
    }

    private boolean isBookingValid() {
        if (booking == null) {
            showToast(getString(R.string.booking_data_is_missing));
            return false;
        }
        return true;
    }

    private String formatTimeSlot(long startTime, long endTime) {
        String timeFormat = "HH:mm";
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat, Locale.getDefault());
        return timeFormatter.format(new Date(startTime)) + " - " + timeFormatter.format(new Date(endTime));
    }

    private String formatDate(long startTime) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormatter.format(new Date(startTime));
    }

    private Transaction createTransaction() {
        double convertedPrice = CurrencyManager.getInstance().convertToCAD(booking.getPrice(), booking.getCurrencyCode());
        return new Transaction(transactionId, booking.getTitle(), convertedPrice, booking.getCurrencySymbol(), DateTimeUtils.getCurrentDateTime(), false);
    }

    private void confirmBookingInService(String transactionId, String selectedTimeSlot, String selectedDate) {
        bookingManager.getBookingService().confirmBooking(
                transactionId,
                selectedTimeSlot,
                selectedDate,
                booking,
                this::onBookingConfirmed,
                this::onBookingConfirmationError
        );
    }

    private void onBookingConfirmed() {
        showToast(getString(R.string.booking_confirmed));
        handlePromoCode();
    }

    private void onBookingConfirmationError(Exception error) {
        showToast("Failed to save booking: " + error.getMessage());
    }

    private void handlePromoCode() {
        String promoCode = promoCodeEditText.getText().toString().trim();
        if (promoCode.isEmpty()) return;

        DatabaseReference promotionsRef = FirebaseDatabaseSingleton.getInstance().getReference("Promotions");
        promotionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    if (promotion != null && promoCode.equals(promotion.getPromoCode())) {
                        markPromoCodeAsUsed();
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getSelectedDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(booking.getStartTime()));
    }

    private void onBookingSuccess() {
        showToast(getString(R.string.booking_confirmed));
        String promoCode = promoCodeEditText.getText().toString().trim();
        if (!promoCode.isEmpty()) {
            PromotionHelper.markPromoCodeAsUsed(promoCode, this);
        }
    }

    private void onBookingFailure(String error) {
        showToast("Failed to save booking: " + error);
    }

    private void handleUnexpectedError(Exception e) {
        showToast("An unexpected error occurred: " + e.getMessage());
    }

    private void updateDateAndTimeTextViews(long startTime, long endTime) {
        String startTimeFormatted = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(startTime));
        String endTimeFormatted = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(endTime));
        String dateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startTime));

        timeTextView.setText(MessageFormat.format("{0} - {1}", startTimeFormatted, endTimeFormatted));
        dateTextView.setText(dateFormatted);
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
}
