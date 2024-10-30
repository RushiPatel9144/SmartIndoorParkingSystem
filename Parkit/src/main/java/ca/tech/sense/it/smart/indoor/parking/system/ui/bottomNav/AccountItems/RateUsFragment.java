package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class RateUsFragment extends Fragment {

    RatingBar ratingBar;
    EditText nameField, phoneField, emailField, feedbackComment;
    Button submitFeedbackButton;

    public RateUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_us, container, false);

        // Initialize UI components
        initializeUIComponents(view);

        // Set up click listener for feedback submission
        setupSubmitButtonClickListener();

        return view;
    }

    //Initializes UI components by linking them to the XML layout elements.
    private void initializeUIComponents(View view) {
        nameField = view.findViewById(R.id.feedback_name);
        phoneField = view.findViewById(R.id.feedback_phone);
        emailField = view.findViewById(R.id.feedback_email);
        ratingBar = view.findViewById(R.id.rating_bar);
        feedbackComment = view.findViewById(R.id.feedback_comment);
        submitFeedbackButton = view.findViewById(R.id.submit_feedback_button);
    }

    // Sets up the submit button click listener with feedback to the user.
    private void setupSubmitButtonClickListener() {
        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmitFeedback();
            }
        });
    }

     //Handles feedback submission with feedback validation and user notification.
    private void handleSubmitFeedback() {
        // Retrieve inputs
        String name = nameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        float rating = ratingBar.getRating();
        String comment = feedbackComment.getText().toString().trim();

        // Validate inputs
        if (isInputValid(name, phone, email, rating)) {
            // Display success message
            showFeedbackSubmittedToast(name, rating);
            clearFeedbackInputs();
        } else {
            showFeedbackErrorToast();
        }
    }

    private boolean isInputValid(String name, String phone, String email, float rating) {
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches()) {
            phoneField.setError("Valid phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Valid email is required");
            return false;
        }
        if (rating <= 0) {
            Toast.makeText(getContext(), "Please select a star rating", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Displays a toast confirming submission of feedback with the selected rating.
    private void showFeedbackSubmittedToast(String name, float rating) {
        Toast.makeText(getContext(), "Thank you, " + name + "! You rated us: " + rating + " stars.", Toast.LENGTH_SHORT).show();
    }


    //Clears the inputs after submission for a clean slate.
    private void clearFeedbackInputs() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        ratingBar.setRating(0);
        feedbackComment.setText("");
    }

    // Displays an error toast when no feedback is provided.
    private void showFeedbackErrorToast() {
        Toast.makeText(getContext(), "Please correct the errors before submitting", Toast.LENGTH_SHORT).show();
    }
}