package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleSlot;
public class AddSlotValidator {

    private AddSlotValidator() {
    }

    public static String isSlotIdValid(String slotId, String emptyMessage) {
        if (slotId == null || slotId.trim().isEmpty()) {
            return emptyMessage;
        }
        return "true";
    }

    public static String isBatteryLevelValid(String batteryLevelStr, String emptyMessage, String invalidMessage, String outOfRangeMessage) {
        if (batteryLevelStr == null || batteryLevelStr.trim().isEmpty()) {
            return emptyMessage;
        }

        try {
            float batteryLevel = Float.parseFloat(batteryLevelStr);
            if (batteryLevel < 0 || batteryLevel > 100) {
                return outOfRangeMessage;
            }
        } catch (NumberFormatException e) {
            return invalidMessage;
        }

        return "true";
    }

    public static String validateSlotInfo(String slotId, String batteryLevelStr, String slotEmptyMessage, String batteryEmptyMessage, String batteryInvalidMessage, String batteryOutOfRangeMessage) {
        String slotValidationResult = isSlotIdValid(slotId, slotEmptyMessage);
        if (!slotValidationResult.equals("true")) {
            return slotValidationResult;
        }

        String batteryValidationResult = isBatteryLevelValid(batteryLevelStr, batteryEmptyMessage, batteryInvalidMessage, batteryOutOfRangeMessage);
        if (!batteryValidationResult.equals("true")) {
            return batteryValidationResult;
        }

        return "true";
    }
}
