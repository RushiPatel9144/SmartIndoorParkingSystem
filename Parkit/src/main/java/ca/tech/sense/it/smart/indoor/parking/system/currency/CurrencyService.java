package ca.tech.sense.it.smart.indoor.parking.system.currency;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class CurrencyService {
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/9ed4766f7af09bae390fba86/latest/";
    private static final String TAG = "CurrencyService";

    public interface Callback {
        void onSuccess(Map<String, Double> exchangeRates);
        void onError(String error);
    }

    public static void fetchLiveRates(Context context, String baseCurrency, Callback callback) {
        if (context != null) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_URL + baseCurrency)
                    .build();

            try {
                client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e(TAG, context.getString(R.string.network_request_failed) + e.getMessage());
                        callback.onError(context.getString(R.string.network_error_unable_to_fetch_exchange_rates_please_try_again_later));
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String jsonResponse = response.body().string();
                            Map<String, Double> rates = parseRates(jsonResponse);
                            callback.onSuccess(rates);
                        } else {
                            Log.e(TAG, context.getString(R.string.api_response_error) + response.message());
                            callback.onError(context.getString(R.string.api_error) + response.message());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, context.getString(R.string.request_failed) + e.getMessage());
                callback.onError(context.getString(R.string.network_error_unable_to_fetch_exchange_rates_please_try_again_later));
            }
        }
    }

    private static Map<String, Double> parseRates(String jsonResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");

            Map<String, Double> rates = new HashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : conversionRates.entrySet()) {
                rates.put(entry.getKey(), entry.getValue().getAsDouble());
            }
            return rates;
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            return Collections.emptyMap();
        }
    }
}
