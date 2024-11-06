package ca.tech.sense.it.smart.indoor.parking.system.model.user;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;

public class UserManager {
    private static final String TAG = "UserManager";
    private static UserManager instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;



    private User currentUser;

    // Private constructor to prevent direct instantiation
    private UserManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Singleton instance
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    // Method to get current user data
    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // Fetch user data from Firestore
    public void fetchUserData(OnUserDataFetchedCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Log.d(TAG, "No authenticated user.");
            callback.onUserDataFetched(null);
            return;
        }

        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentUser = document.toObject(User.class);
                            callback.onUserDataFetched(currentUser);
                        } else {
                            Log.d(TAG, "No user data found in Firestore.");
                            callback.onUserDataFetched(null);
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch user data", task.getException());
                        callback.onUserDataFetched(null);
                    }
                });
    }

    // Sign out the user and clear cached user data
    public void signOut() {
        mAuth.signOut();
        currentUser = null;
    }

    // Interface for callback
    public interface OnUserDataFetchedCallback {
        void onUserDataFetched(User user);
    }
}