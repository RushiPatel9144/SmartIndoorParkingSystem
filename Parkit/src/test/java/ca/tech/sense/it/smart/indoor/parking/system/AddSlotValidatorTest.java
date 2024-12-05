package ca.tech.sense.it.smart.indoor.parking.system;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleSlot.AddSlotValidator;

public class AddSlotValidatorTest {

    private static final String SLOT_EMPTY_MESSAGE = "Slot ID is required";
    private static final String BATTERY_EMPTY_MESSAGE = "Battery level is required";
    private static final String BATTERY_INVALID_MESSAGE = "Invalid battery level format";
    private static final String BATTERY_OUT_OF_RANGE_MESSAGE = "Battery level must be between 0 and 100";

    @Test
    public void testIsSlotIdValid() {
        assertEquals("true", AddSlotValidator.isSlotIdValid("Slot1", SLOT_EMPTY_MESSAGE));
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.isSlotIdValid("", SLOT_EMPTY_MESSAGE));
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.isSlotIdValid("   ", SLOT_EMPTY_MESSAGE));
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.isSlotIdValid(null, SLOT_EMPTY_MESSAGE));
    }

    @Test
    public void testIsBatteryLevelValid() {
        assertEquals("true", AddSlotValidator.isBatteryLevelValid("50", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.isBatteryLevelValid("", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.isBatteryLevelValid("   ", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.isBatteryLevelValid(null, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_OUT_OF_RANGE_MESSAGE, AddSlotValidator.isBatteryLevelValid("-10", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_OUT_OF_RANGE_MESSAGE, AddSlotValidator.isBatteryLevelValid("150", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_INVALID_MESSAGE, AddSlotValidator.isBatteryLevelValid("abc", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
    }

    @Test
    public void testValidateSlotInfo() {
        assertEquals("true", AddSlotValidator.validateSlotInfo("Slot1", "50", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.validateSlotInfo("", "50", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.validateSlotInfo("Slot1", "", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_OUT_OF_RANGE_MESSAGE, AddSlotValidator.validateSlotInfo("Slot1", "200", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
        assertEquals(BATTERY_INVALID_MESSAGE, AddSlotValidator.validateSlotInfo("Slot1", "abc", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE));
    }
}
