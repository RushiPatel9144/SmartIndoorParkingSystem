package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.RateUs;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;


public class RateUsFragment extends Fragment {

    RatingBar ratingBar;
    EditText feedbackComment;
    Button submitFeedbackButton;
    FirebaseFirestore db;
    FirebaseAuth auth;

    public RateUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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
                // Get device model
                String deviceModel = Build.MODEL;

                if (rating > 0 || !comment.isEmpty()) {
                    // Fetch user information from Firestore
                    String uid = auth.getCurrentUser().getUid();
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null) {
                                        String userName = user.getFirstName() + " " + user.getLastName();
                                        String userEmail = user.getEmail();
                                        String userPhone = user.getPhone();

                                        // Create a new RateUs object
                                        RateUs feedback = new RateUs(rating, comment, deviceModel, userName, userEmail, userPhone);

                                        // Add a new document with generated ID to the 'feedback' collection
                                        db.collection("feedback").add(feedback)
                                                .addOnSuccessListener(documentReference -> {
                                                    Toast.makeText(getContext(), "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Error submitting feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        // Clear the inputs after submission (optional)
                                        ratingBar.setRating(0);
                                        feedbackComment.setText("");
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error fetching user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "Please provide a rating or comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
