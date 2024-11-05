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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class RateUsFragment extends Fragment {

    RatingBar ratingBar;
    EditText feedbackComment;
    Button submitFeedbackButton;
    FirebaseFirestore db;

    public RateUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
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
                // Get rating value
                float rating = ratingBar.getRating();
                // Get feedback comment
                String comment = feedbackComment.getText().toString();

                // Display a toast message as a placeholder for feedback submission
                if (rating > 0 || !comment.isEmpty()) {
                    Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();

                    // Create a new feedback object
                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("rating", rating);
                    feedback.put("comment", comment);

                    // Add a new document with generated ID to the 'feedback' collection
                    db.collection("feedback").add(feedback)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(getContext(), "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error submitting feedback", Toast.LENGTH_SHORT).show();
                            });

                    // Clear the inputs after submission (optional)
                    ratingBar.setRating(0);
                    feedbackComment.setText("");

                } else {
                    Toast.makeText(getContext(), "Please provide a rating or comment", Toast.LENGTH_SHORT).show();
                }
             
                handleSubmitFeedback();
            }
        });
    }

     //Handles feedback submission with feedback validation and user notification.
    private void handleSubmitFeedback() {

        float rating = ratingBar.getRating();
        String comment = feedbackComment.getText().toString().trim();

        // Validate inputs
        if (isInputValid(rating)) {
            // Display success message
            showFeedbackSubmittedToast(rating);
            clearFeedbackInputs();
        } else {
            showFeedbackErrorToast();
        }
    }

    private boolean isInputValid(float rating) {
        if (rating <= 0) {
            Toast.makeText(getContext(), "Please select a star rating", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Displays a toast confirming submission of feedback with the selected rating.
    private void showFeedbackSubmittedToast( float rating) {
        Toast.makeText(getContext(), "Thank you ! You rated us: " + rating + " stars.", Toast.LENGTH_SHORT).show();
    }


    //Clears the inputs after submission for a clean slate.
    private void clearFeedbackInputs() {
        ratingBar.setRating(0);
        feedbackComment.setText("");
    }

    // Displays an error toast when no feedback is provided.
    private void showFeedbackErrorToast() {
        Toast.makeText(getContext(), "Please correct the errors before submitting", Toast.LENGTH_SHORT).show();
    }
}