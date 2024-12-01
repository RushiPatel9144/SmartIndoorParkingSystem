package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.COLLECTION_FEEDBACK;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.RateUs;

public class RateUsViewModel extends ViewModel {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RateUsPrefs";
    private static final String LAST_SUBMISSION_TIME = "LastSubmissionTime";
    private static final long TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    public RateUsViewModel(Context context) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void submitFeedback(float rating, String comment, List<String> selectedOptions, String fullDeviceInfo, Context context, FeedbackSubmissionCallback callback, Runnable onSuccess, Runnable onFailure) {
        if (rating == 0) {
            Toast.makeText(context, "Please provide Ratings", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedOptions.isEmpty()) {
            Toast.makeText(context, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar and hide submit button
        callback.showProgressBar();

        // Introduce a delay of 5 seconds
        new Handler().postDelayed(() -> {
            // Fetch user or owner information from SessionManager
            SessionManager sessionManager = SessionManager.getInstance(context);
            sessionManager.fetchSessionData((user, owner) -> {
                if (user != null || owner != null) {
                    // Retrieve the relevant user or owner details
                    String userName = user != null ? user.getFirstName() + " " + user.getLastName() : owner.getFirstName() + " " + owner.getLastName();
                    String userEmail = user != null ? user.getEmail() : owner.getEmail();
                    String userPhone = user != null ? user.getPhone() : owner.getPhone();
                    String userType = user != null ? USER_TYPE_USER : USER_TYPE_OWNER; // Determine user type dynamically

                    // Create a new RateUs object
                    RateUs feedback = new RateUs(rating, comment, fullDeviceInfo, userName, userEmail, userPhone, userType, selectedOptions);

                    // Add a new document with generated ID to the 'feedback' collection
                    db.collection(COLLECTION_FEEDBACK).add(feedback)
                            .addOnSuccessListener(documentReference -> {
                                // Save the current time as the last submission time
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong(LAST_SUBMISSION_TIME, System.currentTimeMillis());
                                editor.apply();
                                onSuccess.run();
                                callback.hideProgressBar();
                            })
                            .addOnFailureListener(e -> {
                                onFailure.run();
                                callback.hideProgressBar();
                            });
                } else {
                    // Handle case where session data is unavailable
                    onFailure.run();
                    callback.hideProgressBar();
                }
            });
        }, 5000); // 5 seconds delay
    }



    public long getLastSubmissionTime() {
        return sharedPreferences.getLong(LAST_SUBMISSION_TIME, 0);
    }

    public long getTwentyFourHours() {
        return TWENTY_FOUR_HOURS;
    }

    // Callback interface for feedback submission
    public interface FeedbackSubmissionCallback {
        void showProgressBar();
        void hideProgressBar();
    }
}
