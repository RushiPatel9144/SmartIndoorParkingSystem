/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.annotation.SuppressLint;
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

import androidx.fragment.app.Fragment;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    @SuppressLint("DefaultLocale")
    public static void showTimedConfirmationDialog(
            Fragment fragment,
            String title,
            String message,
            int timerDuration,
            int interval,
            Runnable confirmAction,
            Runnable cancelAction) {

        // Inflate custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(fragment.requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_leave_app, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        confirmButton.setEnabled(false);
        cancelButton.setText(R.string.cancel);

        AlertDialog dialog = new AlertDialog.Builder(fragment.requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        // Use ScheduledExecutorService for fixed-delay scheduling
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final int[] remainingTime = {timerDuration / 1000}; // seconds
        // Schedule task to update the button text at regular intervals
        scheduler.scheduleWithFixedDelay(() -> {
            if (remainingTime[0] > 0) {
                // Update button text with remaining time
                fragment.requireActivity().runOnUiThread(() -> {
                    // Log for debugging purposes
                    confirmButton.setText(String.format("Confirm (%d)", remainingTime[0]));
                    remainingTime[0]--; // Decrement time after updating text
                });
            } else {
                // Enable the confirm button and update text when timer finishes
                fragment.requireActivity().runOnUiThread(() -> {
                    confirmButton.setText(R.string.confirm);
                    confirmButton.setEnabled(true);
                });
                scheduler.shutdown(); // Stop scheduler after the timer ends
            }
        }, 0, interval, TimeUnit.MILLISECONDS); // Start immediately, repeat every 'interval' milliseconds

        // Cancel button action
        cancelButton.setOnClickListener(v -> {
            scheduler.shutdownNow(); // Stop scheduler immediately
            dialog.dismiss();
            if (cancelAction != null) {
                cancelAction.run();
            }
        });

        // Confirm button action
        confirmButton.setOnClickListener(v -> {
            scheduler.shutdownNow(); // Stop scheduler immediately
            dialog.dismiss();
            if (confirmAction != null) {
                confirmAction.run();
            }
        });
    }

}
