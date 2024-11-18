package ca.tech.sense.it.smart.indoor.parking.system.currency;

public class Currency {
    private String code; // Currency code
    private String symbol; // Currency symbol
    private double exchangeRateToBase; // Exchange rate relative to base currency

    public Currency(String code, String symbol, double exchangeRateToBase) {
        this.code = code;
        this.symbol = symbol;
        this.exchangeRateToBase = exchangeRateToBase;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getExchangeRateToBase() {
        return exchangeRateToBase;
    }

    public void setExchangeRateToBase(double exchangeRateToBase) {
        this.exchangeRateToBase = exchangeRateToBase;
    }
}
