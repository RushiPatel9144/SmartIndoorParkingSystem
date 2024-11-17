package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.owner.OwnerActivity;

public class LauncherUtils {

    // Input validation for email and password
    public static boolean validateInputLogin(EditText emailField, EditText passwordField) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Please enter an email address.");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Please enter a password.");
            return false;
        }
        return true;
    }

    // Navigate to the main activity (User)
    public static void navigateToMainActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    // Navigate to the owner dashboard
    public static void navigateToOwnerDashboard(AppCompatActivity activity) {
        Intent intent = new Intent(activity, OwnerActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    // Show toast message
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
