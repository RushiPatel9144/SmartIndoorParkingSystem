package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;
import android.widget.EditText;

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
        if (locationAddress == null || locationAddress.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
