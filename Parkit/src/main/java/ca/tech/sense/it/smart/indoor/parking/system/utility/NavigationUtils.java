package ca.tech.sense.it.smart.indoor.parking.system.utility;

import androidx.appcompat.app.AppCompatActivity;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LauncherUtils;

public class NavigationUtils {

    public static void navigateToDashboard(AppCompatActivity activity, String userType) {
        if ("owner".equals(userType)) {
            LauncherUtils.navigateToOwnerDashboard(activity);
        } else if ("user".equals(userType)) {
            LauncherUtils.navigateToMainActivity(activity);
        } else {
            LauncherUtils.showToast(activity, "Invalid user type. Please log in.");
            // Optionally log out or redirect to the first screen
        }
    }
}
