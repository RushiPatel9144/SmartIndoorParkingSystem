package ca.tech.sense.it.smart.indoor.parking.system.utility;

import static ca.tech.sense.it.smart.indoor.parking.system.R.string.error_sending_email;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.please_enter_your_email;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.reset_email_sent;


import android.content.Context;
import android.text.TextUtils;
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

    private AuthUtils(){}

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

    // Updated method signature to match the usage with FirebaseAuth
    public static void sendPasswordResetEmail(String email, FirebaseAuth auth, Context context) {
        if (email.isEmpty()) {
            Toast.makeText(context, please_enter_your_email, Toast.LENGTH_SHORT).show();
            return;
        }

        // Send password reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, reset_email_sent, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, error_sending_email, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Updated method to return email using FirebaseAuth instance
    public static String getCurrentUserEmail(FirebaseAuth auth) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getEmail();
        }
        return null;
    }

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



}
