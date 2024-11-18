package ca.tech.sense.it.smart.indoor.parking.system.currency;


import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyPreferenceManager {
    private static final String PREF_NAME = "CurrencyPrefs";
    private static final String KEY_SELECTED_CURRENCY = "selected_currency";
    private final SharedPreferences preferences;

    public CurrencyPreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setSelectedCurrency(String currencyCode) {
        preferences.edit().putString(KEY_SELECTED_CURRENCY, currencyCode).apply();
    }

    public String getSelectedCurrency() {
        return preferences.getString(KEY_SELECTED_CURRENCY, "CAD");
    }
}
