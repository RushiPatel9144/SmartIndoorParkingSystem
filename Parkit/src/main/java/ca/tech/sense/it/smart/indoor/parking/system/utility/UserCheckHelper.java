package ca.tech.sense.it.smart.indoor.parking.system.utility;


import android.content.Context;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;

public class UserCheckHelper {

    private static final String USERS_COLLECTION = "users";
    private static final String OWNERS_COLLECTION = "owners";
    private FirebaseFirestore db;

    public UserCheckHelper() {
        db = FirestoreSingleton.getInstance();
    }

    // Method to check if the user is an owner or regular user
    public void checkUserType(String userId, Context context, UserTypeCallback callback) {
        db.collection(OWNERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onUserTypeDetermined(UserType.OWNER);
                    } else {
                        checkIfUserInUsers(userId, context, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error checking in owners collection", Toast.LENGTH_SHORT).show();
                    callback.onError();
                });
    }

    private void checkIfUserInUsers(String userId, Context context, UserTypeCallback callback) {
        db.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onUserTypeDetermined(UserType.USER);
                    } else {
                        Toast.makeText(context, "User doesn't exist in either collection", Toast.LENGTH_SHORT).show();
                        callback.onError();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error checking in users collection", Toast.LENGTH_SHORT).show();
                    callback.onError();
                });
    }

    // Enum to define user types
    public enum UserType {
        OWNER,
        USER
    }

    // Callback interface for passing the result back to the caller
    public interface UserTypeCallback {
        void onUserTypeDetermined(UserType userType);

        void onError();
    }
}
