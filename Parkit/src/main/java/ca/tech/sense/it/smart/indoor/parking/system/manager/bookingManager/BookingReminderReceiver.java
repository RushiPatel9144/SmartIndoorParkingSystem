package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;

public class BookingReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String userId = intent.getStringExtra("userId");

        NotificationHelper.sendNotification(context, title, message, userId);
    }
}
