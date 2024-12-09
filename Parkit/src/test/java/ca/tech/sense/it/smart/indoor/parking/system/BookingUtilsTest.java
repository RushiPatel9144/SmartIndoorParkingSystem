package ca.tech.sense.it.smart.indoor.parking.system;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

import ca.tech.sense.it.smart.indoor.parking.system.utility.BookingUtils;

public class BookingUtilsTest {


    @Test
    public void testGeneratePassKey() {
        // Test that the generated pass key is a 4-digit string
        String passKey = BookingUtils.generatePassKey();
        assertNotNull(passKey);
        assertEquals("Pass key should be 4 digits long", 4, passKey.length());
        try {
            Integer.parseInt(passKey); // Check if passKey can be converted to an integer
        } catch (NumberFormatException e) {
            fail("Pass key should be a valid integer");
        }
    }



    @Test
    public void testDateFormat() {
        // Test if the date format used in convertToMillis is correct
        String testDate = "2024-12-03 12:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = sdf.parse(testDate);
            assertNotNull(String.valueOf(date), "Parsed date should not be null");
        } catch (Exception e) {
            fail("Date parsing failed");
        }
    }

    @Test
    public void testGeneratePassKey_UniqueKeys() {
        String passKey1 = BookingUtils.generatePassKey();
        String passKey2 = BookingUtils.generatePassKey();
        String passKey3 = BookingUtils.generatePassKey();

        assertNotEquals("Pass keys should be unique", passKey1, passKey2);
        assertNotEquals("Pass keys should be unique", passKey1, passKey3);
        assertNotEquals("Pass keys should be unique", passKey2, passKey3);
    }


    @Test
    public void testConvertToMillis_EmptyDate() {
        String emptyDate = "";
        long millis = BookingUtils.convertToMillis(emptyDate);
        assertEquals("Millis should be 0 for an empty date string", 0, millis);
    }


    @Test
    public void testCalculateDelay_LeapYearDate() {
        String leapYearDate = "2024-02-29 12:00"; // 2024 is a leap year
        long millis = BookingUtils.convertToMillis(leapYearDate);

        assertNotEquals("Millis should be non-zero for a valid leap year date", 0, millis);
    }

    @Test
    public void testCalculateDelay_EndOfDay() {
        String endOfDay = "2024-12-31 23:59";
        long millis = BookingUtils.convertToMillis(endOfDay);

        assertTrue("Millis should be positive for a valid end-of-day time", millis > 0);
    }

    @Test
    public void testCalculateDelay_StartOfDay() {
        String startOfDay = "2024-12-31 00:00";
        long millis = BookingUtils.convertToMillis(startOfDay);

        assertTrue("Millis should be positive for a valid start-of-day time", millis > 0);
    }
}
