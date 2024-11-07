/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.RateUs;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;

public class RateUsFragment extends Fragment {

    RatingBar ratingBar;
    EditText feedbackComment;
    Button submitFeedbackButton;
    TextView optionParkingSpot, optionSecureTransaction, optionUserInterface, optionRealTime;
    FirebaseFirestore db;
    FirebaseAuth auth;
    List<String> selectedOptions;

    public RateUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        selectedOptions = new ArrayList<>();
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

    // Initializes UI components by linking them to the XML layout elements.
    private void initializeUIComponents(View view) {
        ratingBar = view.findViewById(R.id.rating_bar);
        feedbackComment = view.findViewById(R.id.feedback_comment);
        submitFeedbackButton = view.findViewById(R.id.submit_feedback_button);
        optionParkingSpot = view.findViewById(R.id.option_parking_spot);
        optionSecureTransaction = view.findViewById(R.id.option_secure_transaction);
        optionUserInterface = view.findViewById(R.id.option_user_interface);
        optionRealTime = view.findViewById(R.id.option_real_time);

        // Set up option click listeners
        setOptionClickListener(optionParkingSpot, getString(R.string.parking_spot_easily_found));
        setOptionClickListener(optionSecureTransaction, getString(R.string.secure_transaction));
        setOptionClickListener(optionUserInterface, getString(R.string.user_friendly_interface));
        setOptionClickListener(optionRealTime, getString(R.string.real_time_features));
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
                // Get device model
                String deviceModel = Build.MODEL;
                String manufacturer = Build.MANUFACTURER;
                String fullDeviceInfo = manufacturer + " " + deviceModel;

                if (rating > 0 || !comment.isEmpty() || !selectedOptions.isEmpty()) {
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
                                        RateUs feedback = new RateUs(rating, comment, fullDeviceInfo, userName, userEmail, userPhone, selectedOptions);

                                        // Add a new document with generated ID to the 'feedback' collection
                                        db.collection("feedback").add(feedback)
                                                .addOnSuccessListener(documentReference -> {
                                                    Toast.makeText(getContext(), getString(R.string.feedback_submitted_successfully), Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), getString(R.string.error_submitting_feedback) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        // Clear the inputs after submission (optional)
                                        ratingBar.setRating(0);
                                        feedbackComment.setText("");
                                        clearSelectedOptions();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), getString(R.string.error_fetching_user_info) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), getString(R.string.please_provide_a_rating_comment_or_select_an_option), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setOptionClickListener(TextView optionView, String optionText) {
        optionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedOptions.contains(optionText)) {
                    selectedOptions.remove(optionText);
                    optionView.setBackgroundResource(R.drawable.box_background);
                } else {
                    selectedOptions.add(optionText);
                    optionView.setBackgroundResource(R.drawable.box_background_selected);
                }
            }
        });
    }

    private void clearSelectedOptions() {
        selectedOptions.clear();
        optionParkingSpot.setBackgroundResource(R.drawable.box_background);
        optionSecureTransaction.setBackgroundResource(R.drawable.box_background);
        optionUserInterface.setBackgroundResource(R.drawable.box_background);
        optionRealTime.setBackgroundResource(R.drawable.box_background);
    }
}
