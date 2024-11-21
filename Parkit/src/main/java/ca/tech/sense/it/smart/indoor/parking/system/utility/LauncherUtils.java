package ca.tech.sense.it.smart.indoor.parking.system.utility;

import static android.provider.Settings.System.getString;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.regex_password_format_guide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.Manager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.owner.OwnerActivity;

public class LauncherUtils {

    private static FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

    public static boolean validateInputLogin(EditText emailField, EditText passwordField) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        boolean isValid = true;

        // Email validation
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Please enter an email address.");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format.");
            isValid = false;
        } else {
            emailField.setError(null); // Clear error if valid
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Please enter a password.");
            isValid = false;
        } else {
            passwordField.setError(null); // Clear error if valid
        }

        return isValid;
    }

    // Helper function to validate password with regex
    private static boolean isValidPassword(String password) {
        // At least 8 characters, one uppercase, one lowercase, one digit, and one special character
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$";
        return password.matches(passwordRegex);
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

    public static void navigateBasedOnUserType(String userType, Context context) {
        SessionManager sessionManager = new SessionManager(context);

        // Check if user is logged in based on the user type
        String authToken = sessionManager.getAuthToken();

        if ("owner".equals(userType)) {
            if (authToken != null) {
                navigateToOwnerDashboard((AppCompatActivity) context);
            } else {
                showToast(context, "Owner session not found. Please log in again.");
            }
        } else if ("user".equals(userType)) {
            if (authToken != null) {
                navigateToMainActivity((AppCompatActivity) context);
            } else {
                showToast(context, "User session not found. Please log in again.");
            }
        } else {
            showToast(context, "Invalid session. Please log in again.");
        }
    }

    // Save Owner Data to Firestore
    public static void saveOwnerToFirestore(Context context, String userID, String fName, String lName, String email, String phoneNumber) {
        DocumentReference ownerRef = fireStore.collection("owners").document(userID);

        // First check if the owner document already exists
        ownerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // If the document exists, update only specific fields
                ownerRef.update("fName", fName, "lName", lName, "email", email, "phoneNumber", phoneNumber)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LauncherUtils", "Owner profile updated successfully: " + userID);
                            navigateToOwnerDashboard((AppCompatActivity) context);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LauncherUtils", "Error updating owner data: " + e.getMessage());
                            showToast(context, "Failed to update owner data.");
                        });
            } else {
                // If the document doesn't exist, create a new one
                Owner localOwner = new Owner(userID, fName, lName, email, phoneNumber, null);
                ownerRef.set(localOwner)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LauncherUtils", "Owner profile created successfully: " + userID);
                            navigateToOwnerDashboard((AppCompatActivity) context);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LauncherUtils", "Error saving owner data: " + e.getMessage());
                            showToast(context, "Failed to save owner data.");
                        });
            }
        }).addOnFailureListener(e -> {
            Log.e("LauncherUtils", "Error checking owner data: " + e.getMessage());
            showToast(context, "Failed to check owner data.");
        });
    }

    // Save User Data to Firestore
    public static void saveUserToFirestore(Context context, String userID, String fName, String lName, String email, String phoneNumber) {
        DocumentReference userRef = fireStore.collection("users").document(userID);

        // First check if the user document already exists
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // If the document exists, update only specific fields
                userRef.update("fName", fName, "lName", lName, "email", email, "phoneNumber", phoneNumber)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LauncherUtils", "User profile updated successfully: " + userID);
                            navigateToMainActivity((AppCompatActivity) context);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LauncherUtils", "Error updating user data: " + e.getMessage());
                            showToast(context, "Failed to update user data.");
                        });
            } else {
                // If the document doesn't exist, create a new one
                User localUser = new User(userID, fName, lName, email, phoneNumber, null);
                userRef.set(localUser)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("LauncherUtils", "User profile created successfully: " + userID);
                            navigateToMainActivity((AppCompatActivity) context);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LauncherUtils", "Error saving user data: " + e.getMessage());
                            showToast(context, "Failed to save user data.");
                        });
            }
        }).addOnFailureListener(e -> {
            Log.e("LauncherUtils", "Error checking user data: " + e.getMessage());
            showToast(context, "Failed to check user data.");
        });
    }

    // Validates input fields for sign-up (with EditText components)
    public static boolean validateInput(Context context, EditText firstName, EditText lastName, EditText email, EditText phone,
                                        EditText password, EditText confirmPassword, CheckBox checkBox,
                                        String fName, String lName, String emailInput, String passwordInput,
                                        String confirmPasswordInput, String phoneNumber) {

        // Clear previous errors
        firstName.setError(null);
        lastName.setError(null);
        email.setError(null);
        phone.setError(null);
        password.setError(null);
        confirmPassword.setError(null);

        // 1. First Name Validation
        if (TextUtils.isEmpty(fName)) {
            firstName.setError(context.getString(R.string.please_enter_your_first_name));
            return false;
        }

        // 2. Last Name Validation
        if (TextUtils.isEmpty(lName)) {
            lastName.setError(context.getString(R.string.please_enter_your_last_name));
            return false;
        }

        // 3. Email Validation
        if (TextUtils.isEmpty(emailInput)) {
            email.setError(context.getString(R.string.enter_e_mail));
            return false;
        }
        // Email regex to validate the email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!emailInput.matches(emailRegex)) {
            email.setError(context.getString(R.string.invalid_email_format));
            return false;
        }

        // 4. Phone Number Validation
        if (TextUtils.isEmpty(phoneNumber)) {
            phone.setError(context.getString(R.string.enter_phone_number));
            return false;
        }
        // Regex to validate phone number (for simplicity, ensuring it's 10 digits)
        String phoneRegex = "^[0-9]{10}$";
        if (!phoneNumber.matches(phoneRegex)) {
            phone.setError(context.getString(R.string.invalid_phone_number));
            return false;
        }

        // 5. Password Validation
        if (TextUtils.isEmpty(passwordInput)) {
            password.setError(context.getString(R.string.enter_passwords));
            return false;
        }
        // Password regex to ensure strong password (at least 8 characters, 1 upper, 1 lower, 1 number, 1 special)
        String passwordRegex = context.getString(R.string.regex_string);
        if (!passwordInput.matches(passwordRegex)) {
            password.setError(context.getString(regex_password_format_guide));

            // Display Toast with password format description
            String passwordFormatToast = context.getString(regex_password_format_guide);
            Toast.makeText(context, passwordFormatToast, Toast.LENGTH_LONG).show();
            return false;
        }

        // 6. Confirm Password Validation
        if (TextUtils.isEmpty(confirmPasswordInput)) {
            confirmPassword.setError(context.getString(R.string.confirm_password_is_required));
            return false;
        }
        if (!passwordInput.equals(confirmPasswordInput)) {
            confirmPassword.setError(context.getString(R.string.passwords_do_not_match));
            return false;
        }

        // 7. Terms and Conditions Checkbox Validation
        if (!checkBox.isChecked()) {
            Toast.makeText(context, context.getString(R.string.sign_please_accept_the_terms_and_conditions), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}


