package ca.tech.sense.it.smart.indoor.parking.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.testng.annotations.Test;
import ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleLocation.AddLocationValidator;

public class AddLocationValidatorTest {

    @Before
    public void setUp() throws Exception {
        // executed before each test case
    }

    @After
    public void shutdown() {
        // executed after each test case
    }


    // Test for isLocationNameValid()
    @Test
    public void testIsLocationNameValid() {
        assertTrue(AddLocationValidator.isLocationNameValid("Valid Location"));
        assertFalse(AddLocationValidator.isLocationNameValid(""));       // Empty string
        assertFalse(AddLocationValidator.isLocationNameValid("   "));    // String with spaces
        assertFalse(AddLocationValidator.isLocationNameValid(null));     // Null value
    }

    // Test for isPostalCodeValid()
    @Test
    public void testIsPostalCodeValid() {
        assertTrue(AddLocationValidator.isPostalCodeValid("12345"));
        assertFalse(AddLocationValidator.isPostalCodeValid(""));      // Empty string
        assertFalse(AddLocationValidator.isPostalCodeValid("   "));   // String with spaces
        assertFalse(AddLocationValidator.isPostalCodeValid(null));    // Null value
    }

    // Test for isPriceValid()
    @Test
    public void testIsPriceValid() {
        assertTrue(AddLocationValidator.isPriceValid("19.99"));
        assertFalse(AddLocationValidator.isPriceValid(""));           // Empty string
        assertFalse(AddLocationValidator.isPriceValid("   "));        // String with spaces
        assertFalse(AddLocationValidator.isPriceValid("-10.00"));     // Negative price
        assertFalse(AddLocationValidator.isPriceValid("abc"));        // Invalid format
        assertFalse(AddLocationValidator.isPriceValid(null));         // Null value
    }

    // Test for isLocationAddressValid()
    @Test
    public void testIsLocationAddressValid() {
        assertTrue(AddLocationValidator.isLocationAddressValid("123 Street Name"));
        assertFalse(AddLocationValidator.isLocationAddressValid(""));          // Empty string
        assertFalse(AddLocationValidator.isLocationAddressValid("   "));       // String with spaces
        assertFalse(AddLocationValidator.isLocationAddressValid(null));        // Null value
    }

}
