package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.signUp;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.FirestoreHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.InputValidatorHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.ToastHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login.LoginActivity;

public class SignUpHelper {

    /**
     * Navigates the user to the Login screen.
     *
     * @param activity The current activity context.
     * @param userType The type of user (e.g., "user" or "owner").
     */
    public static void navigateToLogin(AppCompatActivity activity, String userType) {
        Intent loginIntent = new Intent(activity, LoginActivity.class);
        loginIntent.putExtra("userType", userType);
        activity.startActivity(loginIntent);
        activity.finish();
    }

    /**
     * Scrolls the view to ensure the password field is visible when focus changes.
     *
     * @param hasFocus          Indicates whether the password field has focus.
     * @param editTextPassword  The password input field.
     * @param scrollView        The parent scroll view containing the input fields.
     */
    public static void scrollViewChangeBasedOnUi(Boolean hasFocus, EditText editTextPassword,EditText editTextConfirmPassword, View scrollView) {
        if (!hasFocus) {
            scrollView.scrollTo(0, editTextConfirmPassword.getBottom());
        }
    }

    /**
     * Handles the sign-up process for new users or owners.
     * Performs input validation, Firebase authentication, and Firestore data storage.
     *
     * @param progressBar       The progress bar to show while processing.
     * @param editTextEmail     Input field for the email.
     * @param editTextPassword  Input field for the password.
     * @param editTextConfirmPassword Input field for password confirmation.
     * @param firstName         Input field for the first name.
     * @param lastName          Input field for the last name.
     * @param phone             Input field for the phone number.
     * @param checkBox          Terms and conditions agreement checkbox.
     * @param userType          The type of user (e.g., "user" or "owner").
     * @param mAuth             FirebaseAuth instance for authentication.
     * @param activity          The current activity context.
     */
    public static void handleSignUp(
            ProgressBar progressBar,
            EditText editTextEmail,
            EditText editTextPassword,
            EditText editTextConfirmPassword,
            EditText firstName,
            EditText lastName,
            EditText phone,
            CheckBox checkBox,
            String userType,
            FirebaseAuth mAuth,
            AppCompatActivity activity,
            TextInputLayout signup_email_layout,
            TextInputLayout signup_password_layout,
            TextInputLayout signup_confirm_password_layout,
            TextInputLayout signup_phone_layout,
            TextInputLayout signup_firstname_layout,
            TextInputLayout signup_lastname_layout
    ) {
        progressBar.setVisibility(View.VISIBLE);

        // Extract input data from EditText fields
        String email = getText(editTextEmail);
        String password = getText(editTextPassword);
        String confirmPassword = getText(editTextConfirmPassword);
        String fName = getText(firstName);
        String lName = getText(lastName);
        String phoneNumber = getText(phone);

        // Validate user input; if invalid, stop further processing
        if (!InputValidatorHelper.validateInputSignUp(
                activity,checkBox,fName, lName, email, password, confirmPassword, phoneNumber, signup_email_layout,signup_password_layout,signup_confirm_password_layout,signup_phone_layout,signup_firstname_layout,signup_lastname_layout

        )) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Perform Firebase authentication for user sign-up
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Get the unique user ID from Firebase
                        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        Log.d("SignUpActivity", "Firebase Auth success, UID: " + userID);

                        // Store user or owner information in Firestore
                        saveUserToFirestore(userType, activity, userID, fName, lName, email, phoneNumber);
                    } else {
                        // Log and display error message on sign-up failure
                        String errorMsg = Objects.requireNonNull(task.getException()).getMessage();
                        Log.e("SignUpActivity", "Firebase Auth failed: " + errorMsg);
                        ToastHelper.showToast(activity, errorMsg);
                    }
                });
    }

    /**
     * Helper method to extract trimmed text from an EditText.
     *
     * @param editText The EditText to extract text from.
     * @return A trimmed string from the EditText.
     */
    private static String getText(EditText editText) {
        return editText.getText().toString().trim();
    }

    /**
     * Saves user or owner information to Firestore based on the user type.
     *
     * @param userType     The type of user ("user" or "owner").
     * @param activity     The current activity context.
     * @param userID       The unique Firebase user ID.
     * @param fName        The user's first name.
     * @param lName        The user's last name.
     * @param email        The user's email.
     * @param phoneNumber  The user's phone number.
     */
    private static void saveUserToFirestore(
            String userType,
            AppCompatActivity activity,
            String userID,
            String fName,
            String lName,
            String email,
            String phoneNumber
    ) {
        if ("owner".equals(userType)) {
            // Save owner-specific data to Firestore
            FirestoreHelper.saveOwnerToFirestore(activity, userID, fName, lName, email, phoneNumber);
        } else {
            // Save user-specific data to Firestore
            FirestoreHelper.saveUserToFirestore(activity, userID, fName, lName, email, phoneNumber);
        }
    }

}
