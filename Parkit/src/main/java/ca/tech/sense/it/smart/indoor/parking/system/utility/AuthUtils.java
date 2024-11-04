package ca.tech.sense.it.smart.indoor.parking.system.utility;


import static ca.tech.sense.it.smart.indoor.parking.system.R.string.error_sending_email;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.please_enter_your_email;
import static ca.tech.sense.it.smart.indoor.parking.system.R.string.reset_email_sent;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
}
