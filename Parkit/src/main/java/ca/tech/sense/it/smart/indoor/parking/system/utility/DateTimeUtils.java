package ca.tech.sense.it.smart.indoor.parking.system.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    /**
     * Gets the current date and time as a formatted string.
     *
     * @return Current date and time in "yyyy-MM-dd HH:mm:ss" format.
     */
    public static String getCurrentDateTime() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
