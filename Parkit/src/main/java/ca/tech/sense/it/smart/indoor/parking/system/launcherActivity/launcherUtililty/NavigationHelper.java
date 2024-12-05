package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import ca.tech.sense.it.smart.indoor.parking.system.userUi.UserMainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login.LoginActivity;
import ca.tech.sense.it.smart.indoor.parking.system.ownerUi.OwnerActivity;

public class NavigationHelper {

    private NavigationHelper(){}

    public static void navigateToMainActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, UserMainActivity.class);
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

    /**
     * Called from FirstActivity to navigate to the appropriate activity based on the user type for auto login.
     @param userType: String representing the user type.
     @param context: Context of the calling activity.
     **/
    public static void navigateBasedOnUserType(String userType, Context context) {
        switch (userType) {
            case USER_TYPE_OWNER:
                NavigationHelper.navigateToOwnerDashboard((AppCompatActivity) context);
                break;
            case USER_TYPE_USER:
                NavigationHelper.navigateToMainActivity((AppCompatActivity) context);
                break;
            default:
                ToastHelper.showToast(context, String.valueOf(R.string.unrecognized_user_type_please_log_in_again));
        }
    }
}
