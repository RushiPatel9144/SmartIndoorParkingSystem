/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Objects;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class DialogUtil {

    // Callback interface for input dialog
    public interface InputDialogCallback {
        void onConfirm(String inputText);
        void onCancel();
    }

    // Callback interface for message dialog
    public interface DialogCallback {
        void onConfirm();
        void onCancel();
    }

    // New callback interface for confirmation dialog
    public interface ConfirmDialogCallback {
        void onConfirm();
    }

    public interface BackPressCallback{
        void onConfirm();
        void onCancel();
    }

    // Method to show an input dialog
    public static void showInputDialog(Context context, String title, String hint, InputDialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        EditText dialogInput = dialogView.findViewById(R.id.dialog_input);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);

        dialogTitle.setText(title);
        dialogInput.setHint(hint);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onCancel();
            }
        });

        confirmButton.setOnClickListener(v -> {
            String inputText = dialogInput.getText().toString().trim();
            dialog.dismiss();
            if (callback != null) {
                callback.onConfirm(inputText);
            }
        });

        dialog.show();
    }

    // Method to show a message dialog
    public static void showMessageDialog(Context context, String title, String message,String confirm, DialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_message, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        confirmButton.setText(confirm);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onCancel();
            }
        });

        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onConfirm();
            }
        });

        dialog.show();
    }

    // New method to show a confirmation dialog with only an OK button
    public static void showConfirmationDialog(Context context, String title, String message, String confirm, ConfirmDialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_confirm, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        confirmButton.setText(confirm);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onConfirm();
            }
        });

        dialog.show();
    }

    // New method to show a confirmation dialog with the email in bold
    public static void showConfirmationDialogWithEmail(Context context, String title, String message, String email, String confirm, ConfirmDialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_confirm, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);

        dialogTitle.setText(title);

        // Create a SpannableString to bold the email
        String fullMessage = message + "\n\nWe will reach you out soon on " + email;
        SpannableString spannableString = new SpannableString(fullMessage);
        int start = fullMessage.indexOf(email);
        int end = start + email.length();
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        dialogMessage.setText(spannableString);
        confirmButton.setText(confirm);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onConfirm();
            }
        });

        dialog.show();
    }

// Method to show a confirmation dialog asking if the user is sure they want to leave the app
    public static void showLeaveAppDialog(Context context, String title, String message, int imageResource, BackPressCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_leave_app, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        ImageView dialogImage = dialogView.findViewById(R.id.dialog_image);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        // Set the title and message
        dialogTitle.setText(title);
        dialogMessage.setText(message);

        // Set the image resource (icon or any image you want to display)
        dialogImage.setImageResource(imageResource);

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        // Set custom background for the dialog window
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);

        // Show the dialog once, after it's fully configured
        dialog.show();

        // Handle Confirm button click
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onConfirm();
            }
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onCancel();
            }
        });
        dialog.show();
    }

}
