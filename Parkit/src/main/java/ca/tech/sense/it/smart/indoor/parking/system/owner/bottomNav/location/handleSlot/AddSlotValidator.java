package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleSlot;
import android.widget.EditText;

public class AddSlotValidator {

    private AddSlotValidator() {
        // Prevent instantiation
    }

    public static boolean isSlotIdValid(EditText slotIdField, String errorMessage) {
        String slotId = slotIdField.getText().toString().trim();
        if (slotId.isEmpty()) {
            slotIdField.setError(errorMessage);
            slotIdField.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isBatteryLevelValid(EditText batteryLevelField, String emptyMessage, String invalidMessage, String outOfRangeMessage) {
        String batteryLevelStr = batteryLevelField.getText().toString().trim();
        if (batteryLevelStr.isEmpty()) {
            batteryLevelField.setError(emptyMessage);
            batteryLevelField.requestFocus();
            return false;
        }

        try {
            float batteryLevel = Float.parseFloat(batteryLevelStr);
            if (batteryLevel < 0 || batteryLevel > 100) {
                batteryLevelField.setError(outOfRangeMessage);
                batteryLevelField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            batteryLevelField.setError(invalidMessage);
            batteryLevelField.requestFocus();
            return false;
        }

        return true;
    }
}
