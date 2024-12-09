package ca.tech.sense.it.smart.indoor.parking.system.logic;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.MessageFormat;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.Help;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class HelpFragmentLogic {

    private Context context;
    private final EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etComment;
    private Button btnSubmitHelp;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String TAG = "HelpFragmentLogic :";

    public HelpFragmentLogic(Context context, EditText etName, EditText etPhone, EditText etEmail, EditText etComment, Button btnSubmitHelp, ProgressBar progressBar) {
        this.context = context;
        this.etName = etName;
        this.etPhone = etPhone;
        this.etEmail = etEmail;
        this.etComment = etComment;
        this.btnSubmitHelp = btnSubmitHelp;
        this.progressBar = progressBar;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    public void fetchUserData() {
        SessionManager sessionManager = SessionManager.getInstance(context);
        String userType = sessionManager.getUserType();

        if (userType == null || (!userType.equals(USER_TYPE_USER) && !userType.equals(USER_TYPE_OWNER))) {
            Log.e(TAG, "Invalid user type: " + userType);
            Toast.makeText(context, context.getString(R.string.invalid_user_type), Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the correct object based on userType
        Object sessionData = USER_TYPE_USER.equals(userType) ? sessionManager.getCurrentUser() : sessionManager.getCurrentOwner();

        if (sessionData != null) {
            populateUserData(sessionData);  // Populate UI with the session data
        } else {
            Log.e(TAG, userType + " data is missing from the session.");
            Toast.makeText(context, userType + context.getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void populateUserData(Object sessionData) {
        String firstName = null, lastName = null, phone = null, email = null;

        if (sessionData instanceof User) {
            User user = (User) sessionData;
            firstName = user.getFirstName();
            lastName = user.getLastName();
            phone = user.getPhone();
            email = user.getEmail();
        } else if (sessionData instanceof Owner) {
            Owner owner = (Owner) sessionData;
            firstName = owner.getFirstName();
            lastName = owner.getLastName();
            phone = owner.getPhone();
            email = owner.getEmail();
        }

        // Populate UI fields
        if (firstName != null && lastName != null) {
            etName.setText(MessageFormat.format("{0} {1}", firstName, lastName));
        }
        if (phone != null) {
            etPhone.setText(phone);
        }
        if (email != null) {
            etEmail.setText(email);
        }
    }


    public void submitHelpRequest() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String comment = etComment.getText().toString().trim();

        // Validations
        if (name.isEmpty()) {
            etName.setError(context.getString(R.string.please_fill_the_name));
            etName.requestFocus();
        } else if (phone.isEmpty()) {
            etPhone.setError(context.getString(R.string.please_fill_the_phone_number));
            etPhone.requestFocus();
        } else if (email.isEmpty()) {
            etEmail.setError(context.getString(R.string.please_fill_the_email_address));
            etEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(context.getString(R.string.please_enter_a_valid_email_address));
            etEmail.requestFocus();
        } else if (comment.isEmpty()) {
            etComment.setError(context.getString(R.string.please_describe_the_issue));
            etComment.requestFocus();
        } else {
            // Show progress bar and hide submit button
            progressBar.setVisibility(View.VISIBLE);
            btnSubmitHelp.setVisibility(View.GONE);

            // Simulate delay of 5 seconds
            new Handler().postDelayed(() -> {
                // Create a new Help object
                Help help = new Help(name, phone, email, comment);

                // Add the Help object to the 'help' collection
                db.collection("help").add(help)
                        .addOnSuccessListener(documentReference -> {
                            // Clear the fields
                            etName.setText("");
                            etPhone.setText("");
                            etEmail.setText("");
                            etComment.setText("");

                            // Show confirmation dialog
                            String message = context.getString(R.string.help_request_submitted);
                            DialogUtil.showConfirmationDialogWithEmail(context, context.getString(R.string.confirmation), message, email, context.getString(R.string.ok), () -> {
                                // Dialog will be dismissed automatically
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Show error message
                            String errorMessage = context.getString(R.string.error_submitting_help_request) + e.getMessage();
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        });

                // Hide progress bar and show submit button
                progressBar.setVisibility(View.GONE);
                btnSubmitHelp.setVisibility(View.VISIBLE);
            }, 5000); // 5 seconds delay
        }
    }
}

