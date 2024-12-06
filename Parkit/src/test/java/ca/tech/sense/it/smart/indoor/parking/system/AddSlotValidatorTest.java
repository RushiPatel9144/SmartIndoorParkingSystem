package ca.tech.sense.it.smart.indoor.parking.system;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleSlot.AddSlotValidator;

public class AddSlotValidatorTest {

    private static final String SLOT_EMPTY_MESSAGE = "Slot ID is required";
    private static final String BATTERY_EMPTY_MESSAGE = "Battery level is required";
    private static final String BATTERY_INVALID_MESSAGE = "Invalid battery level format";
    private static final String BATTERY_OUT_OF_RANGE_MESSAGE = "Battery level must be between 0 and 100";

    // Slot ID Validation Tests
    @Test
    public void testValidSlotId() {
        assertEquals("true", AddSlotValidator.isSlotIdValid("Slot1", SLOT_EMPTY_MESSAGE)); // Valid Slot ID
    }

    @Test
    public void testEmptySlotId() {
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.isSlotIdValid("", SLOT_EMPTY_MESSAGE)); // Empty input
    }

    @Test
    public void testWhitespaceSlotId() {
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.isSlotIdValid("   ", SLOT_EMPTY_MESSAGE)); // Whitespace only
    }

    @Test
    public void testNullSlotId() {
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.isSlotIdValid(null, SLOT_EMPTY_MESSAGE)); // Null input
    }

    // Battery Level Validation Tests
    @Test
    public void testValidBatteryLevel() {
        assertEquals("true", AddSlotValidator.isBatteryLevelValid("75", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Valid input
    }

    @Test
    public void testEmptyBatteryLevel() {
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.isBatteryLevelValid("", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Empty input
    }

    @Test
    public void testWhitespaceBatteryLevel() {
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.isBatteryLevelValid("   ", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Whitespace only
    }

    @Test
    public void testBatteryLevelOutOfRangeLow() {
        assertEquals(BATTERY_OUT_OF_RANGE_MESSAGE, AddSlotValidator.isBatteryLevelValid("-5", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Negative value
    }

    @Test
    public void testBatteryLevelOutOfRangeHigh() {
        assertEquals(BATTERY_OUT_OF_RANGE_MESSAGE, AddSlotValidator.isBatteryLevelValid("150", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Above max limit
    }

    @Test
    public void testBatteryLevelInvalidFormat() {
        assertEquals(BATTERY_INVALID_MESSAGE, AddSlotValidator.isBatteryLevelValid("ABC", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Alphabetic input
    }

    @Test
    public void testBatteryLevelWithDecimal() {
        assertEquals("true", AddSlotValidator.isBatteryLevelValid("50.5", BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Valid decimal
    }

    // Slot Info Validation Tests
    @Test
    public void testValidSlotInfo() {
        assertEquals("true", AddSlotValidator.validateSlotInfo("Slot1", "85", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // All valid inputs
    }

    @Test
    public void testInvalidSlotInfoEmptySlotId() {
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.validateSlotInfo("", "85", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Empty Slot ID
    }

    @Test
    public void testInvalidSlotInfoEmptyBatteryLevel() {
        assertEquals(BATTERY_EMPTY_MESSAGE, AddSlotValidator.validateSlotInfo("Slot1", "", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Empty battery level
    }

    @Test
    public void testInvalidSlotInfoOverRangeBattery() {
        assertEquals(BATTERY_OUT_OF_RANGE_MESSAGE, AddSlotValidator.validateSlotInfo("Slot1", "200", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Battery out of range
    }

    @Test
    public void testInvalidSlotInfoInvalidBatteryFormat() {
        assertEquals(BATTERY_INVALID_MESSAGE, AddSlotValidator.validateSlotInfo("Slot1", "invalid", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Invalid battery format
    }

    @Test
    public void testInvalidSlotInfoBothFieldsInvalid() {
        assertEquals(SLOT_EMPTY_MESSAGE, AddSlotValidator.validateSlotInfo("", "invalid", SLOT_EMPTY_MESSAGE, BATTERY_EMPTY_MESSAGE, BATTERY_INVALID_MESSAGE, BATTERY_OUT_OF_RANGE_MESSAGE)); // Both invalid
    }
}
