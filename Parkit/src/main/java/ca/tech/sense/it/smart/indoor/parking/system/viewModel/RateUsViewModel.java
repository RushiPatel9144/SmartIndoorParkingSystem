package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import ca.tech.sense.it.smart.indoor.parking.system.model.RateUs;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

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
            // Fetch user information from Firestore
            String uid = auth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
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
                        }
                    })
                    .addOnFailureListener(e -> {
                        onFailure.run();
                        callback.hideProgressBar();
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
