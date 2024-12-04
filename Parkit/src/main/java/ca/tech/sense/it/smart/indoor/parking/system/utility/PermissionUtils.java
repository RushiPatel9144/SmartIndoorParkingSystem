package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

public class PermissionUtils {

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void requestExactAlarmPermission(Context context) {
        if (!context.getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                new AlertDialog.Builder(context)
                        .setTitle("Exact Alarm Permission Required")
                        .setMessage("This app requires permission to schedule exact alarms. Please grant this permission in the settings.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }
}

