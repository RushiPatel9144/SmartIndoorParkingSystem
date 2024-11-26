package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login.LoginActivity;
import ca.tech.sense.it.smart.indoor.parking.system.owner.OwnerActivity;

public class NavigationHelper {

    public static void navigateToMainActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void navigateToOwnerDashboard(AppCompatActivity activity) {
        Intent intent = new Intent(activity, OwnerActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void navigateToLoginFromFirst(String userType, AppCompatActivity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("userType", userType);
        activity.startActivity(intent);
        activity.finish();
    }
}
