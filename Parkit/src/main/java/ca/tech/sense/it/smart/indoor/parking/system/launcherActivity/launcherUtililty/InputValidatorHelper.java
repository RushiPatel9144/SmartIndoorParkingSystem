package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import static ca.tech.sense.it.smart.indoor.parking.system.R.string.regex_password_format_guide;

import android.content.Context;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class InputValidatorHelper {

    // Validates input fields for sign-up (with EditText components)
    public static boolean validateInputSignUp(Context context, EditText firstName, EditText lastName, EditText email, EditText phone,
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
}
