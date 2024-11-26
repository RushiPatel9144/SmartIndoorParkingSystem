package ca.tech.sense.it.smart.indoor.parking.system.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuthSingleton.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginWithToken(String token) {
        return firebaseAuth.signInWithCustomToken(token);
    }

    public Task<DocumentSnapshot> checkOwner(String userId) {
        return firestore.collection("owners").document(userId).get();
    }
    public Task<DocumentSnapshot> checkUser(String userId) {
        return firestore.collection("users").document(userId).get();
    }

    // Add this method to check if the email exists in a given Firestore collection
    public Task<Boolean> isEmailRegisteredInCollection(String email, String collection) {
        return firestore.collection(collection)
                .whereEqualTo("email", email) // Assuming 'email' is a field in your Firestore documents
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty(); // Returns true if the email is found
                    } else {
                        throw Objects.requireNonNull(task.getException()); // Rethrow any exception that occurred
                    }
                });
    }

    // Send a password reset email
    public Task<Void> sendPasswordResetEmail(String email) {
        return FirebaseAuth.getInstance().sendPasswordResetEmail(email);
    }


    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
