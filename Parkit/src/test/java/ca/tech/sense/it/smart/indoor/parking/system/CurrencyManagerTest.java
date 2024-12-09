package ca.tech.sense.it.smart.indoor.parking.system;

import org.junit.Before;
import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;
import ca.tech.sense.it.smart.indoor.parking.system.currency.Currency;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;

public class CurrencyManagerTest {

    private CurrencyManager currencyManager;

    @Before
    public void setUp() {
        currencyManager = CurrencyManager.getInstance();
    }

    @Test
    public void testConvertFromCAD_ValidRate() {
        // Set exchange rate for USD
        Currency usd = currencyManager.getCurrency("USD");
        usd.setExchangeRateToBase(0.75);  // USD to CAD rate

        // Test the conversion
        double amountInCAD = 100.0;
        double convertedAmount = currencyManager.convertFromCAD(amountInCAD, "USD");

        // Assert that the conversion is correct
        assertEquals(75.0, convertedAmount, 0.01);  // 100 CAD * 0.75 = 75 USD
    }

    @Test
    public void testConvertFromCAD_InvalidRate() {
        // Set exchange rate to 0 for EUR (invalid)
        Currency eur = currencyManager.getCurrency("EUR");
        eur.setExchangeRateToBase(0.0);

        double amountInCAD = 100.0;
        double convertedAmount = currencyManager.convertFromCAD(amountInCAD, "EUR");

        // Assert that it falls back to the original amount
        assertEquals(100.0, convertedAmount, 0.01);  // Should return original amount due to invalid rate
    }

    @Test
    public void testConvertFromCAD_NonexistentCurrency() {
        double amountInCAD = 100.0;
        double convertedAmount = currencyManager.convertFromCAD(amountInCAD, "XYZ");

        assertEquals(amountInCAD, convertedAmount, 0.01);  // Fallback to original amount
    }


    @Test
    public void testConvertFromCAD_NegativeAmount() {
        Currency usd = currencyManager.getCurrency("USD");
        usd.setExchangeRateToBase(0.75);

        double amountInCAD = -100.0;
        double convertedAmount = currencyManager.convertFromCAD(amountInCAD, "USD");

        assertEquals(-75.0, convertedAmount, 0.01);  // -100 CAD * 0.75 = -75 USD
    }


    @Test
    public void testUpdateExchangeRates() {
        Map<String, Double> newRates = Map.of(
                "USD", 0.75,
                "EUR", 0.85
        );

        // Update exchange rates in CurrencyManager
        currencyManager.updateExchangeRates(newRates);

        // Verify the exchange rates are updated
        Currency usd = currencyManager.getCurrency("USD");
        Currency eur = currencyManager.getCurrency("EUR");

        assertNotNull(usd);
        assertEquals(0.75, usd.getExchangeRateToBase(), 0.01);

        assertNotNull(eur);
        assertEquals(0.85, eur.getExchangeRateToBase(), 0.01);
    }

    @Test
    public void testConvertFromCAD_Precision() {
        Currency usd = currencyManager.getCurrency("USD");
        usd.setExchangeRateToBase(0.123456);

        double amountInCAD = 100.0;
        double convertedAmount = currencyManager.convertFromCAD(amountInCAD, "USD");

        assertEquals(12.35, convertedAmount, 0.01);  // Rounded to 2 decimal places
    }

    // Test conversion when no exchange rate is set
    @Test
    public void testConvertFromCAD_NoRateSet() {
        double amountInCAD = 100.0;
        double convertedAmount = currencyManager.convertFromCAD(amountInCAD, "GBP");

        assertEquals(amountInCAD, convertedAmount, 0.01);  // No rate means fallback to original amount
    }


}
