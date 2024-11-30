package ca.tech.sense.it.smart.indoor.parking.system.currency;

import java.util.HashMap;
import java.util.Map;

public class CurrencyManager {
    private static CurrencyManager instance;
    private final Map<String, Currency> currencies;
    private final String baseCurrency = "CAD";  // Default base currency

    private CurrencyManager() {
        currencies = new HashMap<>();
        loadDefaultCurrencies();
    }

    public static CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    private void loadDefaultCurrencies() {
        currencies.put("CAD", new Currency("CAD", "CAD$", 1.0));  // Base currency with rate 1.0
        currencies.put("USD", new Currency("USD", "USD$", 0.0));  // USD Dollar
        currencies.put("EUR", new Currency("EUR", "€", 0.0));  // Euro
        currencies.put("INR", new Currency("INR", "₹", 0.0));  // Indian Rupees
        currencies.put("GBP", new Currency("GBP", "£", 0.0));  // British Pound
        currencies.put("AUD", new Currency("AUD", "A$", 0.0));  // Australian Dollar
    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public Currency getCurrency(String code) {
        return currencies.get(code);
    }

    public void fetchAndUpdateRates(CurrencyService.Callback callback) {
        // Fetch live rates and update exchange rates
        CurrencyService.fetchLiveRates(baseCurrency, new CurrencyService.Callback() {
            @Override
            public void onSuccess(Map<String, Double> exchangeRates) {
                updateExchangeRates(exchangeRates);
                callback.onSuccess(exchangeRates);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // Method to update the exchange rates for each currency
    public void updateExchangeRates(Map<String, Double> newRates) {
        for (Map.Entry<String, Double> entry : newRates.entrySet()) {
            Currency currency = currencies.get(entry.getKey());
            if (currency != null) {
                currency.setExchangeRateToBase(entry.getValue());
            }
        }

        // Ensure base currency rate is always 1.0
        setBaseCurrencyRateToOne();
    }

    private void setBaseCurrencyRateToOne() {
        Currency base = currencies.get(baseCurrency);
        if (base != null) {
            base.setExchangeRateToBase(1.0);
        }
    }

    // Method to convert from CAD to selected currency
    public double convertFromCAD(double amountInCAD, String targetCurrencyCode) {
        Currency targetCurrency = currencies.get(targetCurrencyCode);
        if (targetCurrency != null) {
            double exchangeRate = targetCurrency.getExchangeRateToBase();
            if (exchangeRate > 0.00) {
                return amountInCAD * exchangeRate;
            }
        }
        return amountInCAD;
    }

    public double convertToCAD(double amountInTargetCurrency, String targetCurrencyCode) {
        Currency targetCurrency = currencies.get(targetCurrencyCode);
        if (targetCurrency != null) {
            double exchangeRate = targetCurrency.getExchangeRateToBase();
            if (exchangeRate > 0.00) {
                return amountInTargetCurrency / exchangeRate;
            }
        }
        return amountInTargetCurrency;
    }

}
