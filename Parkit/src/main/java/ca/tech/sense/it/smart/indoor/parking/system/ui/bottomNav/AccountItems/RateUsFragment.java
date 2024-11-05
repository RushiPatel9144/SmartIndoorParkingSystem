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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_us, container, false);

        // Initialize UI components
        ratingBar = view.findViewById(R.id.rating_bar);
        feedbackComment = view.findViewById(R.id.feedback_comment);
        submitFeedbackButton = view.findViewById(R.id.submit_feedback_button);

        // Set up the submit button click listener
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
            }
        });

        return view;
    }
}