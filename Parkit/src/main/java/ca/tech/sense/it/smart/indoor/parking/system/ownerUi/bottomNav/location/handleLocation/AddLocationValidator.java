package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleLocation;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class AddLocationValidator {
    private AddLocationValidator(){
        //empty constructor
    }

    public static boolean isLocationNameValid(EditText locationName, String emptyNameMessage) {
        String locationNameStr = locationName.getText().toString().trim();
        if (locationNameStr.isEmpty()) {
            locationName.setError(emptyNameMessage);
            locationName.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isPostalCodeValid(EditText postalCode, String emptyCodeMessage) {
        String postalCodeStr = postalCode.getText().toString().trim();
        if (postalCodeStr.isEmpty()) {
            postalCode.setError(emptyCodeMessage);
            postalCode.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isPriceValid(EditText price, String emptyPriceMessage, String invalidPriceMessage, String negativePriceMessage) {
        String priceStr = price.getText().toString().trim();
        if (priceStr.isEmpty()) {
            price.setError(emptyPriceMessage);
            price.requestFocus();
            return false;
        }

        try {
            double priceValue = Double.parseDouble(priceStr);
            if (priceValue < 0.00) {
                price.setError(negativePriceMessage);
                price.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            price.setError(invalidPriceMessage);
            price.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isLocationAddressValid(String locationAddress) {
        return locationAddress != null && !locationAddress.trim().isEmpty();
    }

    public static double validatePrice(Context context, String input) {
        String priceStr = input.trim();

        if (priceStr.isEmpty()) {
            Toast.makeText(context, R.string.price_cannot_be_empty , Toast.LENGTH_SHORT).show();
            return -1;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                Toast.makeText(context, R.string.price_cannot_be_negative , Toast.LENGTH_SHORT).show();
                return -1;
            }
            return price;
        } catch (NumberFormatException e) {
            Toast.makeText(context, R.string.invalid_price_format_please_enter_a_valid_number , Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

}
