package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
    EditText feedbackComment;
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
        float rating = ratingBar.getRating();
        String comment = feedbackComment.getText().toString();

        if (isFeedbackValid(rating, comment)) {
            showFeedbackSubmittedToast(rating);
            clearFeedbackInputs();
        } else {
            showFeedbackErrorToast();
        }
    }

    //Validates if feedback is provided by checking rating or comment.
    private boolean isFeedbackValid(float rating, String comment) {
        return rating > 0 || !comment.isEmpty();
    }

    //Displays a toast confirming submission of feedback with the selected rating.
    private void showFeedbackSubmittedToast(float rating) {
        Toast.makeText(getContext(), " You rated " + rating + " stars.", Toast.LENGTH_SHORT).show();
    }

    //Clears the inputs after submission for a clean slate.
    private void clearFeedbackInputs() {
        ratingBar.setRating(0);
        feedbackComment.setText("");
    }

    // Displays an error toast when no feedback is provided.
    private void showFeedbackErrorToast() {
        Toast.makeText(getContext(), "Please provide a rating or comment", Toast.LENGTH_SHORT).show();
    }
}