package ca.tech.sense.it.smart.indoor.parking.system.currency;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CurrencyService {
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/9ed4766f7af09bae390fba86/latest/";

    public interface Callback {
        void onSuccess(Map<String, Double> exchangeRates);
        void onError(String error);
    }

    public static void fetchLiveRates(String baseCurrency, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL + baseCurrency)
                .build();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("CurrencyService", "Network request failed: " + e.getMessage());
                callback.onError("Network error: Unable to fetch exchange rates. Please try again later.");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Map<String, Double> rates = parseRates(jsonResponse);
                    if (rates != null) {
                        callback.onSuccess(rates);
                    } else {
                        Log.e("CurrencyService", "Failed to parse exchange rates.");
                        callback.onError("Error parsing exchange rates.");
                    }
                } else {
                    Log.e("CurrencyService", "API response error: " + response.message());
                    callback.onError("API error: " + response.message());
                }
            }

        });
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
            e.printStackTrace();
            return null;
        }
    }
}
