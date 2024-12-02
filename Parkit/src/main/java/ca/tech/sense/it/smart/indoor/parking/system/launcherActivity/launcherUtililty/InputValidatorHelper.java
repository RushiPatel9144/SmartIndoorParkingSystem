package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import android.content.Context;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class InputValidatorHelper {

    private InputValidatorHelper() {}

    // Validates the sign-up fields
    public static boolean validateInputSignUp(Context context,
                                              CheckBox checkBox,
                                              String fName,
                                              String lName,
                                              String emailInput,
                                              String passwordInput,
                                              String confirmPasswordInput,
                                              String phoneNumber,
                                              TextInputLayout signupEmailLayout,
                                              TextInputLayout signupPasswordLayout,
                                              TextInputLayout signupConfirmPasswordLayout,
                                              TextInputLayout signupPhoneLayout,
                                              TextInputLayout signupFirstnameLayout,
                                              TextInputLayout signupLastnameLayout) {

        // Reset previous errors
        resetErrors(signupFirstnameLayout, signupLastnameLayout, signupEmailLayout, signupPhoneLayout, signupPasswordLayout, signupConfirmPasswordLayout);

        // Validate fields
        if (!isValidFirstName(context, fName, signupFirstnameLayout)) return false;
        if (!isValidLastName(context, lName, signupLastnameLayout)) return false;
        if (!isValidEmail(context, emailInput, signupEmailLayout)) return false;
        if (!isValidPhoneNumber(context, phoneNumber, signupPhoneLayout)) return false;
        if (!isValidPassword(context, passwordInput, signupPasswordLayout)) return false;
        if (!isValidConfirmPassword(context, passwordInput, confirmPasswordInput, signupConfirmPasswordLayout)) return false;
        return isTermsAccepted(context, checkBox);
    }

    // Validates the login fields
    public static boolean validateInputLogin(EditText emailField, EditText passwordField, TextInputLayout loginEmailLayout, TextInputLayout loginPasswordLayout) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        boolean isValid = true;

        // Email validation
        if (TextUtils.isEmpty(email)) {
            loginEmailLayout.setError("Please enter an email address.");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmailLayout.setError("Invalid email format.");
            isValid = false;
        } else {
            loginEmailLayout.setError(null); // Clear error if valid
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            loginPasswordLayout.setError("Please enter a password.");
            isValid = false;
        } else if (password.length() < 8) {
            loginPasswordLayout.setError("Password must be at least 8 characters long.");
        } else {
            loginPasswordLayout.setError(null); // Clear error if valid
        }

        return isValid;
    }

    // Helper Methods

    private static void resetErrors(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            layout.setError(null);
        }
    }

    private static boolean isValidFirstName(Context context, String fName, TextInputLayout signupFirstnameLayout) {
        if (TextUtils.isEmpty(fName)) {
            signupFirstnameLayout.setError(context.getString(R.string.please_enter_your_first_name));
            return false;
        }
        return true;
    }

    private static boolean isValidLastName(Context context, String lName, TextInputLayout signupLastnameLayout) {
        if (TextUtils.isEmpty(lName)) {
            signupLastnameLayout.setError(context.getString(R.string.please_enter_your_last_name));
            return false;
        }
        return true;
    }

    private static boolean isValidEmail(Context context, String emailInput, TextInputLayout signupEmailLayout) {
        if (TextUtils.isEmpty(emailInput)) {
            signupEmailLayout.setError(context.getString(R.string.enter_e_mail));
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!emailInput.matches(emailRegex)) {
            signupEmailLayout.setError(context.getString(R.string.invalid_email_format));
            return false;
        }
        return true;
    }

    private static boolean isValidPhoneNumber(Context context, String phoneNumber, TextInputLayout signupPhoneLayout) {
        if (TextUtils.isEmpty(phoneNumber)) {
            signupPhoneLayout.setError(context.getString(R.string.enter_phone_number));
            return false;
        }
        String phoneRegex = "^\\d{10}$";
        if (!phoneNumber.matches(phoneRegex)) {
            signupPhoneLayout.setError(context.getString(R.string.invalid_phone_number));
            return false;
        }
        return true;
    }

    private static boolean isValidPassword(Context context, String passwordInput, TextInputLayout signupPasswordLayout) {
        if (TextUtils.isEmpty(passwordInput)) {
            signupPasswordLayout.setError(context.getString(R.string.enter_passwords));
            return false;
        }
        String passwordRegex = context.getString(R.string.regex_string);
        if (!passwordInput.matches(passwordRegex)) {
            signupPasswordLayout.setError(context.getString(R.string.regex_password_format_guide));
            return false;
        }
        return true;
    }

    private static boolean isValidConfirmPassword(Context context, String passwordInput, String confirmPasswordInput, TextInputLayout signupConfirmPasswordLayout) {
        if (TextUtils.isEmpty(confirmPasswordInput)) {
            signupConfirmPasswordLayout.setError(context.getString(R.string.confirm_password_is_required));
            return false;
        }
        if (!passwordInput.equals(confirmPasswordInput)) {
            signupConfirmPasswordLayout.setError(context.getString(R.string.passwords_do_not_match));
            return false;
        }
        return true;
    }

    private static boolean isTermsAccepted(Context context, CheckBox checkBox) {
        if (!checkBox.isChecked()) {
            Toast.makeText(context, context.getString(R.string.sign_please_accept_the_terms_and_conditions), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
