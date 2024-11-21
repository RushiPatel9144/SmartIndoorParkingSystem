package ca.tech.sense.it.smart.indoor.parking.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import android.text.Editable;
import android.widget.EditText;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;

import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.AddLocationValidator;

public class AddLocationValidatorTest {

    @Mock
    private EditText mockEditText;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockEditText.getEditableText()).thenReturn(new Editable.Factory().newEditable("valid text")); // Use Editable correctly
    }


    // Test: isLocationAddressValid
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withValidAddress() {
        String address = "123 Main Street";
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }

    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withEmptyAddress() {
        String address = "";
        assertFalse(AddLocationValidator.isLocationAddressValid(address));
    }

    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withNullAddress() {
        String address = null;
        assertFalse(AddLocationValidator.isLocationAddressValid(address));
    }


    // Test: isLocationAddressValid with Address Containing Only Numbers
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withOnlyNumbers() {
        String address = "1234567890"; // Numbers only
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }

    // Test: isLocationAddressValid with Address Having Length Exactly at Limit (if a limit exists)
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withMaxLengthAddress() {
        String address = new String(new char[100]).replace("\0", "A");
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }


    // Test: isLocationAddressValid with Address Containing Numeric and Alphabetic Characters
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withAlphaNumericAddress() {
        String address = "123 Main St"; // A mix of numbers and letters
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }


    // Test: isLocationAddressValid with Address Having Only a Single Character
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withSingleCharacterAddress() {
        String address = "A"; // Single character address
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }

    // Test: isLocationAddressValid with Address Containing Common Punctuation
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withCommonPunctuation() {
        String address = "123 Main St, Apt 4B"; // Address with common punctuation (comma, number, etc.)
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }

    // Test: isLocationAddressValid with Address Containing Special Characters
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withSpecialCharacters() {
        String address = "123 Main St, Apt #5"; // Address with special characters like '#' and ','
        assertTrue(AddLocationValidator.isLocationAddressValid(address));
    }

    // Test: isLocationAddressValid with Address Containing Whitespace Only
    @org.junit.Test
    @Test
    public void testIsLocationAddressValid_withWhitespaceOnly() {
        String address = "  "; // Address containing only spaces
        assertFalse(AddLocationValidator.isLocationAddressValid(address));
    }

}
