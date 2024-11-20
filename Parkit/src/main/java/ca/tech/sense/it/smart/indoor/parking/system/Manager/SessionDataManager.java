package ca.tech.sense.it.smart.indoor.parking.system.Manager;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;

public class SessionDataManager {
    private static final String TAG = "SessionDataManager";
    private static SessionDataManager instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    private User currentUser;
    private Owner currentOwner;
    private String userType; // "user" or "owner"

    // Private constructor to prevent direct instantiation
    private SessionDataManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Singleton instance
    public static synchronized SessionDataManager getInstance() {
        if (instance == null) {
            instance = new SessionDataManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Owner getCurrentOwner() {
        return currentOwner;
    }

    public String getUserType() {
        return userType;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        this.userType = "user";
    }

    public void setCurrentOwner(Owner currentOwner) {
        this.currentOwner = currentOwner;
        this.userType = "owner";
    }

    // Fetch user/owner data from Firestore based on the user type
    public void fetchSessionData(OnSessionDataFetchedCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Log.d(TAG, "No authenticated user.");
            callback.onSessionDataFetched(null, null);
            return;
        }

        if ("user".equals(userType)) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                currentUser = document.toObject(User.class);
                                callback.onSessionDataFetched(currentUser, null);
                            } else {
                                Log.d(TAG, "No user data found in Firestore.");
                                callback.onSessionDataFetched(null, null);
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch user data", task.getException());
                            callback.onSessionDataFetched(null, null);
                        }
                    });
        } else if ("owner".equals(userType)) {
            db.collection("owners").document(firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                currentOwner = document.toObject(Owner.class);
                                callback.onSessionDataFetched(null, currentOwner);
                            } else {
                                Log.d(TAG, "No owner data found in Firestore.");
                                callback.onSessionDataFetched(null, null);
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch owner data", task.getException());
                            callback.onSessionDataFetched(null, null);
                        }
                    });
        } else {
            callback.onSessionDataFetched(null, null);
        }
    }

    // Sign out and clear the session data
    public void signOut() {
        mAuth.signOut();
        currentUser = null;
        currentOwner = null;
        userType = null;
    }

    // Interface for callback
    public interface OnSessionDataFetchedCallback {
        void onSessionDataFetched(User user, Owner owner);
    }
}
