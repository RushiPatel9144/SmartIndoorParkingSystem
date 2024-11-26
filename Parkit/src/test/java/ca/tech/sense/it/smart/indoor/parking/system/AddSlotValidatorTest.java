package ca.tech.sense.it.smart.indoor.parking.system;

import android.text.Editable;
import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleSlot.AddSlotValidator;

public class AddSlotValidatorTest {

    private EditText mockSlotIdField;
    private EditText mockBatteryLevelField;

    @Before
    public void setUp() {
        mockSlotIdField = mock(EditText.class);
        mockBatteryLevelField = mock(EditText.class);
    }


    @Test
    public void testIsSlotIdValid_EmptySlotId() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("");
        when(mockSlotIdField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isSlotIdValid(mockSlotIdField, "Slot ID cannot be empty");

        assertFalse(result);
        verify(mockSlotIdField).setError("Slot ID cannot be empty");
        verify(mockSlotIdField).requestFocus();
    }

    @Test
    public void testIsSlotIdValid_ValidSlotId() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("A123");
        when(mockSlotIdField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isSlotIdValid(mockSlotIdField, "Slot ID cannot be empty");

        assertTrue(result);
        verify(mockSlotIdField, never()).setError(anyString());
        verify(mockSlotIdField, never()).requestFocus();
    }

    @Test
    public void testIsSlotIdValid_WhitespaceSlotId() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("    "); // Whitespace only
        when(mockSlotIdField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isSlotIdValid(mockSlotIdField, "Slot ID cannot be empty");

        assertFalse(result);
        verify(mockSlotIdField).setError("Slot ID cannot be empty");
        verify(mockSlotIdField).requestFocus();
    }

    // Test cases for isBatteryLevelValid

    @Test
    public void testIsBatteryLevelValid_EmptyBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertFalse(result);
        verify(mockBatteryLevelField).setError("Battery level cannot be empty");
        verify(mockBatteryLevelField).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_InvalidBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("abc");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertFalse(result);
        verify(mockBatteryLevelField).setError("Invalid battery level");
        verify(mockBatteryLevelField).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_BatteryLevelOutOfRange() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("150");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertFalse(result);
        verify(mockBatteryLevelField).setError("Battery level out of range");
        verify(mockBatteryLevelField).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_ValidBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("75");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertTrue(result);
        verify(mockBatteryLevelField, never()).setError(anyString());
        verify(mockBatteryLevelField, never()).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_NegativeBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("-5");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertFalse(result);
        verify(mockBatteryLevelField).setError("Battery level out of range");
        verify(mockBatteryLevelField).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_ZeroBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("0");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertTrue(result);
        verify(mockBatteryLevelField, never()).setError(anyString());
        verify(mockBatteryLevelField, never()).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_MaxBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("100");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertTrue(result);
        verify(mockBatteryLevelField, never()).setError(anyString());
        verify(mockBatteryLevelField, never()).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_FloatBatteryLevel() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn("49.5");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertTrue(result);
        verify(mockBatteryLevelField, never()).setError(anyString());
        verify(mockBatteryLevelField, never()).requestFocus();
    }

    @Test
    public void testIsBatteryLevelValid_BatteryLevelWithSpaces() {
        Editable mockEditable = mock(Editable.class);
        when(mockEditable.toString()).thenReturn(" 85 ");
        when(mockBatteryLevelField.getText()).thenReturn(mockEditable);

        boolean result = AddSlotValidator.isBatteryLevelValid(mockBatteryLevelField, "Battery level cannot be empty", "Invalid battery level", "Battery level out of range");

        assertTrue(result);
        verify(mockBatteryLevelField, never()).setError(anyString());
        verify(mockBatteryLevelField, never()).requestFocus();
    }
}
