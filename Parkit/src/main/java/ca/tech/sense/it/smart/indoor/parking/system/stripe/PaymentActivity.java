package ca.tech.sense.it.smart.indoor.parking.system.stripe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class PaymentActivity extends AppCompatActivity {
    private PaymentSheet paymentSheet;
    private String currency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        // Initialize the Stripe SDK with your publishable key
        PaymentConfiguration.init(getApplicationContext(), "pk_test_51QJqGPAwqnUq4xSjJQmV0FKH27StbQRdn5jfOtsyFy3tJadwpId67LbynKlh3aonDKstIxv59LWbhFGGlwTTOTJi00QX02RzJp");

        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
                Toast.makeText(this, R.string.payment_successful, Toast.LENGTH_SHORT).show();
            } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
                PaymentSheetResult.Failed result = (PaymentSheetResult.Failed) paymentSheetResult;
                Toast.makeText(this, String.format("%s%s", getString(R.string.payment_failed), result.getError()), Toast.LENGTH_SHORT).show();
            } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
                Toast.makeText(this, R.string.payment_canceled, Toast.LENGTH_SHORT).show();
            }
        });

        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener(v -> fetchClientSecret());
    }

    private void fetchClientSecret() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://parkit-cd4c2ec26f90.herokuapp.com/create-payment-intent";

        int amount = 4500; //  in cents
        currency = "cad";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("amount", amount);
            jsonRequest.put("currency",currency);
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
                runOnUiThread(() -> Toast.makeText(PaymentActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String clientSecret = jsonResponse.getString("clientSecret");

                        // Start the payment process with the obtained clientSecret
                        startPaymentFlow(clientSecret);
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(PaymentActivity.this, "Failed to parse server response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(PaymentActivity.this, "Server error: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void startPaymentFlow(String clientSecret) {

        final PaymentSheet.GooglePayConfiguration googlePayConfiguration =
                new PaymentSheet.GooglePayConfiguration(
                        PaymentSheet.GooglePayConfiguration.Environment.Test,
                        currency
                );

        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration
                .Builder("ParkIt")
                .googlePay(googlePayConfiguration)
                .build();


        // Present the PaymentSheet
        paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
    }

}
