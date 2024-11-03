package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Notification; // Ensure you import your model
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.NotificationsFragment;

public class NotificationHelper {

    private NotificationHelper() {
    }

    public static final String CHANNEL_ID = "your_channel_id";
    public static final String CHANNEL_NAME = "Your Channel Name";
    public static final String CHANNEL_DESCRIPTION = "Your Channel Description";

    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Use high importance for notifications
        );
        channel.setDescription(CHANNEL_DESCRIPTION);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static boolean areNotificationsEnabled(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager != null && notificationManager.areNotificationsEnabled();
    }

    public static void openNotificationSettings(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        context.startActivity(intent);
    }

    public static void sendNotification(Context context, String title, String message, String userId) {
        if (!areNotificationsEnabled(context)) {
            openNotificationSettings(context);
            return;
        }

        // Create a unique ID for the notification
        String notificationId = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
        long timestamp = System.currentTimeMillis();

        // Save the notification to Firebase
        saveNotificationToFirebase(notificationId, title, message, timestamp, userId);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, NotificationsFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        android.app.Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications) // Ensure you have this drawable resource
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (notificationManager != null) {
            notificationManager.notify((int) timestamp, notification); // Use timestamp as ID to avoid overwriting
        }
    }

    private static void saveNotificationToFirebase(String notificationId, String title, String message, long timestamp, String userId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notifications").child(notificationId);
        Notification notification = new Notification(notificationId, title, message, timestamp); // Use your Notification constructor

        notificationsRef.setValue(notification)
                .addOnSuccessListener(aVoid -> Log.d("NotificationHelper", "Notification saved successfully: " + notificationId))
                .addOnFailureListener(e -> Log.e("NotificationHelper", "Failed to save notification: ", e));
    }
}
