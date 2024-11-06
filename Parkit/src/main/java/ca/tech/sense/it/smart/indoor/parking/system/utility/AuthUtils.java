package ca.tech.sense.it.smart.indoor.parking.system.utility;


import static ca.tech.sense.it.smart.indoor.parking.system.R.string.error_sending_email;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.please_enter_your_email;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.reset_email_sent;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class AuthUtils {

    public static void showResetPasswordDialog(Context context, FirebaseAuth auth) {
        String currentEmail = getCurrentUserEmail(auth);
        if (currentEmail != null) {
            DialogUtil.showMessageDialog(context, context.getString(R.string.reset_password),String.format("%s%s", context.getString(R.string.a_password_reset_link_will_be_sent_to), currentEmail)
                     ,context.getString(R.string.confirm) ,
                    new DialogUtil.DialogCallback() {
                        @Override
                        public void onConfirm() {
                            sendPasswordResetEmail(currentEmail, auth, context);
                        }
                        @Override
                        public void onCancel() {
                        }
                    });
        } else {
            Toast.makeText(context, R.string.no_email_found_for_the_current_user, Toast.LENGTH_SHORT).show();
        }
    }



    public static void sendPasswordResetEmail(String email, FirebaseAuth auth, Context context) {
        if (email.isEmpty()) {
            Toast.makeText(context, please_enter_your_email, Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, reset_email_sent, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, error_sending_email, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static String getCurrentUserEmail(FirebaseAuth auth) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getEmail();
        }
        return null;
    }

    // Utility method to hide the keyboard
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Email validation utility
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Password validation utility
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    // Handle Firebase Authentication errors in a common place
    public static void handleAuthError(Context context, Exception exception, EditText emailEditText, EditText passwordEditText) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            emailEditText.setError(context.getString(R.string.user_not_found));
            emailEditText.requestFocus();
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            emailEditText.setError(context.getString(R.string.email_is_already_registered));
            emailEditText.requestFocus();
        } else if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();
            if (errorCode.equals("ERROR_INVALID_CREDENTIAL")) {
                passwordEditText.setError(context.getString(R.string.invaild_credentials));
                passwordEditText.requestFocus();
            } else {
                Toast.makeText(context, context.getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
        }
    }

    // Utility to show/hide ProgressBar
    public static void toggleProgressBar(ProgressBar progressBar, boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    // Utility method for showing Toast messages
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Firebase Sign In method - Reusable for Google Sign-In or Email/Password sign-in
    public static void signInWithFirebaseAuth(Context context, String email, String password, FirebaseAuth mAuth, ProgressBar progressBar, Runnable onSuccess) {
        toggleProgressBar(progressBar, true);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            toggleProgressBar(progressBar, false);
            if (task.isSuccessful()) {
                onSuccess.run();
            } else {
                handleAuthError(context, task.getException(), null, null);
            }
        });
    }

    // Method for Firebase SignIn using Google

    public static void signInWithGoogle(Context context, FirebaseAuth mAuth, GoogleSignInAccount account, Runnable onSuccess) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onSuccess.run();
            } else {
                Toast.makeText(context, context.getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
