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

                    // Clear the inputs after submission (optional)
                    ratingBar.setRating(0);
                    feedbackComment.setText("");

                    // Here, you could add additional code to send feedback to your server or handle it as needed
                } else {
                    Toast.makeText(getContext(), "Please provide a rating or comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}