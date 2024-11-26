package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import static ca.tech.sense.it.smart.indoor.parking.system.R.string.regex_password_format_guide;

import android.content.Context;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class InputValidatorHelper {

    // Validates input fields for sign-up (with EditText components)
    public static boolean validateInputSignUp(Context context,
                                              CheckBox checkBox,
                                              String fName,
                                              String lName,
                                              String emailInput,
                                              String passwordInput,
                                              String confirmPasswordInput,
                                              String phoneNumber,
                                              TextInputLayout signup_email_layout,
                                              TextInputLayout signup_password_layout,
                                              TextInputLayout signup_confirm_password_layout,
                                              TextInputLayout signup_phone_layout,
                                              TextInputLayout signup_firstname_layout,
                                              TextInputLayout signup_lastname_layout) {

        // Clear previous errors
        signup_firstname_layout.setError(null);
        signup_lastname_layout.setError(null);
        signup_email_layout.setError(null);
        signup_phone_layout.setError(null);
        signup_password_layout.setError(null);
        signup_confirm_password_layout.setError(null);

        // 1. First Name Validation
        if (TextUtils.isEmpty(fName)) {
            signup_firstname_layout.setError(context.getString(R.string.please_enter_your_first_name));
            return false;
        }

        // 2. Last Name Validation
        if (TextUtils.isEmpty(lName)) {
            signup_lastname_layout.setError(context.getString(R.string.please_enter_your_last_name));
            return false;
        }

        // 3. Email Validation
        if (TextUtils.isEmpty(emailInput)) {
            signup_email_layout.setError(context.getString(R.string.enter_e_mail));
            return false;
        }
        // Email regex to validate the email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!emailInput.matches(emailRegex)) {
            signup_email_layout.setError(context.getString(R.string.invalid_email_format));
            return false;
        }

        // 4. Phone Number Validation
        if (TextUtils.isEmpty(phoneNumber)) {
            signup_phone_layout.setError(context.getString(R.string.enter_phone_number));
            return false;
        }
        // Regex to validate phone number (for simplicity, ensuring it's 10 digits)
        String phoneRegex = "^[0-9]{10}$";
        if (!phoneNumber.matches(phoneRegex)) {
            signup_phone_layout.setError(context.getString(R.string.invalid_phone_number));
            return false;
        }

        // 5. Password Validation
        if (TextUtils.isEmpty(passwordInput)) {
            signup_password_layout.setError(context.getString(R.string.enter_passwords));
            return false;
        }
        // Password regex to ensure strong password (at least 8 characters, 1 upper, 1 lower, 1 number, 1 special)
        String passwordRegex = context.getString(R.string.regex_string);
        if (!passwordInput.matches(passwordRegex)) {
            signup_password_layout.setError(context.getString(regex_password_format_guide));

            return false;
        }

        // 6. Confirm Password Validation
        if (TextUtils.isEmpty(confirmPasswordInput)) {
            signup_confirm_password_layout.setError(context.getString(R.string.confirm_password_is_required));
            return false;
        }
        if (!passwordInput.equals(confirmPasswordInput)) {
            signup_confirm_password_layout.setError(context.getString(R.string.passwords_do_not_match));
            return false;
        }

        // 7. Terms and Conditions Checkbox Validation
        if (!checkBox.isChecked()) {
            Toast.makeText(context, context.getString(R.string.sign_please_accept_the_terms_and_conditions), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static boolean validateInputLogin(EditText emailField, EditText passwordField, TextInputLayout login_email_layout, TextInputLayout login_password_layout) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        boolean isValid = true;

        // Email validation
        if (TextUtils.isEmpty(email)) {
            login_email_layout.setError("Please enter an email address.");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            login_email_layout.setError("Invalid email format.");
            isValid = false;
        } else {
            login_email_layout.setError(null); // Clear error if valid
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            login_password_layout.setError("Please enter a password.");
            isValid = false;
        } else if (password.length() < 8) {
            login_password_layout.setError("Password must be at least 8 characters long.");
        }else {
            login_password_layout.setError(null); // Clear error if valid
        }

        return isValid;
    }
}
