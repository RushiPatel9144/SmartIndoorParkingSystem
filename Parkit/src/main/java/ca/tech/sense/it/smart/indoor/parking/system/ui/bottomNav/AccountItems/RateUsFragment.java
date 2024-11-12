/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.RateUs;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class RateUsFragment extends Fragment {

    RatingBar ratingBar;
    EditText feedbackComment;
    Button submitFeedbackButton;
    TextView optionParkingSpot, optionSecureTransaction, optionUserInterface, optionRealTime;
    FirebaseFirestore db;
    FirebaseAuth auth;
    List<String> selectedOptions;
    SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RateUsPrefs";
    private static final String LAST_SUBMISSION_TIME = "LastSubmissionTime";
    private static final long TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

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
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_us, container, false);

        // Initialize UI components
        initializeUIComponents(view);

        // Check if the user can submit feedback
        long lastSubmissionTime = sharedPreferences.getLong(LAST_SUBMISSION_TIME, 0);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSubmissionTime < TWENTY_FOUR_HOURS) {
            long remainingTime = TWENTY_FOUR_HOURS - (currentTime - lastSubmissionTime);
            startTimer(remainingTime);
            submitFeedbackButton.setEnabled(false);
            submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        }

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

                // Validate inputs
                if (rating == 0) {
                    Toast.makeText(getContext(), "Please provide Ratings", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedOptions.isEmpty()) {
                    Toast.makeText(getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show progress bar and hide submit button
                ProgressBar progressBar = getView().findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.VISIBLE);
                submitFeedbackButton.setVisibility(View.GONE);


                // Introduce a delay of 5 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
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
                                                        progressBar.setVisibility(View.GONE);
                                                        submitFeedbackButton.setVisibility(View.VISIBLE);
                                                        // Clear the inputs after submission
                                                        ratingBar.setRating(0);
                                                        feedbackComment.setText("");
                                                        clearSelectedOptions();
                                                        // Save the current time as the last submission time
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putLong(LAST_SUBMISSION_TIME, System.currentTimeMillis());
                                                        editor.apply();
                                                        // Show confirmation dialog
                                                        DialogUtil.showConfirmationDialog(getActivity(), "Confirmation", getString(R.string.feedback_submitted_successfully), getString(R.string.ok), new DialogUtil.ConfirmDialogCallback() {
                                                            @Override
                                                            public void onConfirm() {
                                                                // Dialog will be dismissed automatically
                                                            }
                                                        });
                                                        // Disable the submit button and start the timer
                                                        submitFeedbackButton.setEnabled(false);
                                                        submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                                                        startTimer(TWENTY_FOUR_HOURS);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        submitFeedbackButton.setVisibility(View.VISIBLE);
                                                        Toast.makeText(getContext(), getString(R.string.error_submitting_feedback) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });

                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    submitFeedbackButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), getString(R.string.error_fetching_user_info) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }, 5000); // 5 seconds delay
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

    private void startTimer(long duration) {
        new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                submitFeedbackButton.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                submitFeedbackButton.setEnabled(true);
                submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme));
                submitFeedbackButton.setText(R.string.submit_feedback);
            }
        }.start();
    }
}
