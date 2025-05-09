package ca.tech.sense.it.smart.indoor.parking.system.manager.notificationManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

public class NotificationManagerHelper {
    private static final String CHANNEL_ID = "default";
    private static  Context context;
    private final SharedPreferences sharedPreferences;
    private final FirebaseAuth firebaseAuth;
    private final NotificationManager notificationManager;

    public NotificationManagerHelper(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);
    }

    public void enableNotifications() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle(context.getString(R.string.notifications_enabled))
                .setContentText(context.getString(R.string.you_will_receive_notifications))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    public void disableNotifications() {
        notificationManager.cancelAll();
    }

    public void sendWelcomeBackNotification() {
        long lastSentTimestamp = sharedPreferences.getLong("welcome_notification_timestamp", 0);
        long currentTime = System.currentTimeMillis();
        long cooldown = 24L * 60 * 60 * 1000; // 24 hours in milliseconds

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && (currentTime - lastSentTimestamp > cooldown)) {
            NotificationHelper.sendNotification(
                    context,
                    context.getString(R.string.welcome_back),
                    context.getString(R.string.we_ve_missed_you_check_out_the_latest_parking_spots_available_for_you),
                    currentUser.getUid()
            );
            sharedPreferences.edit().putLong("welcome_notification_timestamp", currentTime).apply();
        }
    }

    public void sendNewUserWelcomeNotification() {
        boolean isWelcomeNotificationSent = sharedPreferences.getBoolean("welcome_notification_sent", false);

        if (!isWelcomeNotificationSent) {
            NotificationHelper.sendNotification(
                    context,
                    context.getString(R.string.welcome_to_parkit),
                    context.getString(R.string.explore_the_app_and_find_parking_spots_nearby),
                    Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()
            );

            sharedPreferences.edit().putBoolean("welcome_notification_sent", true).apply();
        }
    }

    public static void sendBookingConfirmationNotification(String userId, Booking booking, String selectedDate, String[] times) {
        String title = context.getString(R.string.booking_confirmed);
        String message = context.getString(R.string.your_booking_at) + booking.getLocation() + context.getString(R.string.is_confirmed_for) + selectedDate + context.getString(R.string.from) + times[0] + context.getString(R.string.to) + times[1];
        NotificationHelper.sendNotification(context, title, message, userId);
    }

}


