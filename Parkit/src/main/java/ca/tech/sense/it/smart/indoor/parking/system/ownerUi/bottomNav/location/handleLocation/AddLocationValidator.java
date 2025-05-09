package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleLocation;

public class AddLocationValidator {

    private AddLocationValidator() {
        // Empty constructor
    }

    public static boolean isLocationNameValid(String locationName) {
        return locationName != null && !locationName.trim().isEmpty();
    }

    public static boolean isPostalCodeValid(String postalCode) {
        return postalCode != null && !postalCode.trim().isEmpty();
    }

    public static boolean isPriceValid(String price) {
        if (price == null || price.trim().isEmpty()) {
            return false;
        }
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue < 0.00) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isLocationAddressValid(String locationAddress) {
        return locationAddress != null && !locationAddress.trim().isEmpty();
    }

}
