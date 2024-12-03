package ca.tech.sense.it.smart.indoor.parking.system.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class BookingUtils {

    /**
     * Generates a random 4-digit pass key.
     *
     * @return A 4-digit pass key as a String.
     */
    public static String generatePassKey() {
        Random random = new Random();
        int passKey = 1000 + random.nextInt(9000); // Generates a random 4-digit number
        return String.valueOf(passKey);
    }

    /**
     * Converts a date and time string to milliseconds.
     *
     * @param dateTime The date and time string in the format "yyyy-MM-dd HH:mm".
     * @return The corresponding time in milliseconds.
     */
    public static long convertToMillis(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Calculates the delay for scheduling the status update.
     *
     * @param dateTime The date and time string in the format "yyyy-MM-dd HH:mm".
     * @return The delay in milliseconds.
     */
    public static long calculateDelay(String dateTime) {
        long endTimeMillis = convertToMillis(dateTime);
        long currentTimeMillis = System.currentTimeMillis();
        return endTimeMillis - currentTimeMillis;
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
