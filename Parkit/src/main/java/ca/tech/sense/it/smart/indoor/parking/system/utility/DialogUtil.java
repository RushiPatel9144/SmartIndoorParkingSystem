package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    public static void showMessageDialog(Context context, String title, String message, DialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_message, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);

        dialogTitle.setText(title);
        dialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
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
}
