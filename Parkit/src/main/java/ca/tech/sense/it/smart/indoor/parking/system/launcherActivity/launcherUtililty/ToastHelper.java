package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
    // Show toast message
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}


