package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.InputValidatorHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.NavigationHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.ToastHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.FirstActivity;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.signUp.SignUpActivity;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class LoginHelper {

    /**
     * 1. Handle login button click.
     * <p>
     * This method handles the login button click event by validating the email and password input.
     * If valid, it triggers the login process in the ViewModel and displays a progress bar while waiting for a response.
     * </p>
     *
     * @param editTextEmail    The email input field.
     * @param editTextPassword The password input field.
     * @param progressBar      The progress bar to show during login.
     * @param loginViewModel   The ViewModel responsible for managing login logic.
     * @param userType         The type of user ("user" or "owner").
     */
    public static void handleLogin(Context context, EditText editTextEmail, EditText editTextPassword, ProgressBar progressBar, LoginViewModel loginViewModel, String userType, TextInputLayout login_email_layout, TextInputLayout login_password_layout) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate the input and proceed with login if valid
        if (InputValidatorHelper.validateInputLogin(context, editTextEmail, editTextPassword,login_email_layout,login_password_layout)) {
            progressBar.setVisibility(View.VISIBLE);  // Show progress bar while logging in
            loginViewModel.login(email, password, userType);  // Trigger login in ViewModel
        }
    }

    public static void handleSuccessfulLogin(String authToken, String userType, MaterialCheckBox rememberMeCheckBox, SessionManager sessionManager, AppCompatActivity activity) {
        sessionManager.saveAuthToken(authToken, userType, rememberMeCheckBox.isChecked());
        Log.e("LoginHelper",authToken);

        if (USER_TYPE_USER.equals(userType)) {
            NavigationHelper.navigateToMainActivity(activity);
        } else if (USER_TYPE_OWNER.equals(userType)) {
            NavigationHelper.navigateToOwnerDashboard(activity);
        }
    }

    /**
     * 2. Navigate to the SignUpActivity.
     * <p>
     * This method is called when the user clicks on the "Sign Up" link. It navigates to the SignUpActivity and passes the user type.
     * </p>
     *
     * @param activity The current activity from which the navigation will happen.
     * @param userType The type of user ("user" or "owner").
     */
    public static void navigateToSignUp(AppCompatActivity activity, String userType) {
        Intent intent = new Intent(activity, SignUpActivity.class);
        intent.putExtra("userType", userType);  // Pass the user type to SignUpActivity
        activity.startActivity(intent);
        activity.finish();  // Close the current activity
    }

    /**
     * 4. Show the forgot password dialog.
     * <p>
     * This method displays an input dialog asking the user to enter their registered email for password reset.
     * If the email is valid, the password reset request is triggered in the ViewModel.
     * </p>
     *
     * @param context        The activity context.
     * @param loginViewModel The LoginViewModel to handle password reset logic.
     * @param userType       The type of user ("user" or "owner").
     */
    public static void showForgotPasswordDialog(Context context, LoginViewModel loginViewModel, String userType) {
        // Show an input dialog to capture the registered email for password reset
        DialogUtil.showInputDialog(context,
                context.getString(R.string.enter_your_registered_email),
                context.getString(R.string.someone_mail_com),
                new DialogUtil.InputDialogCallback() {
                    @Override
                    public void onConfirm(String inputText) {
                        // Validate the input email and send password reset email if valid
                        if (TextUtils.isEmpty(inputText)) {
                            ToastHelper.showToast(context, context.getString(R.string.email_cannot_be_empty));  // Show error if email is empty
                        } else {
                            loginViewModel.sendPasswordResetEmail(context, inputText, userType);  // Trigger password reset in ViewModel
                        }
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing if the user cancels the dialog
                    }
                });
    }

    public static void navigateToFirst(LoginActivity loginActivity) {
        Intent intent = new Intent(loginActivity, FirstActivity.class);
        loginActivity.startActivity(intent);
        loginActivity.finish();
    }
}
