package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.Constants.USER_TYPE_USER;

import android.content.Context;
import android.util.Log;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.CoroutineHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.NavigationHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;


public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<String> loginStatus = new MutableLiveData<>();
    private final MutableLiveData<String> resetPasswordStatus = new MutableLiveData<>();


    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<String> getLoginStatus() {
        return loginStatus;
    }
    public LiveData<String> getResetPasswordStatus() {
        return resetPasswordStatus;
    }


    public void login(String email, String password, String userType) {
        authRepository.login(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        user.getIdToken(true).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String authToken = task.getResult().getToken();
                                if ("owner".equals(userType)) {
                                    checkIfOwner(user.getUid(),"token:" + authToken); // Pass the token
                                } else {
                                    loginStatus.setValue("token:" + authToken); // User login
                                }
                            } else {
                                loginStatus.setValue("error:" + Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> loginStatus.setValue("error:" + e.getMessage()));
    }

    private void checkIfOwner(String userId, String authToken) {
        authRepository.checkOwner(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loginStatus.setValue("token:" + authToken); // Owner login
                    } else {
                        loginStatus.setValue("error:Not an owner. Please sign up.");
                    }
                });
    }
    // Method to send a password reset email
    public void sendPasswordResetEmail(String email,String userType) {
        // Use Firestore singleton to get the instance
        FirebaseFirestore db = FirestoreSingleton.getInstance();

        // Determine the collection to query based on the user type
        String collection = "user".equals(userType) ? "users" : "owners";

        // Query Firestore to check if the email exists in the appropriate collection
        db.collection(collection)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if the email exists in Firestore
                        if (!task.getResult().isEmpty()) {
                            // Email exists, proceed to send password reset email
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnSuccessListener(aVoid -> {
                                        // Email sent successfully
                                        resetPasswordStatus.setValue("Password reset email sent successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error in sending password reset email
                                        resetPasswordStatus.setValue("Error sending password reset email: " + e.getMessage());
                                    });
                        } else {
                            // Email does not exist in the Firestore collection
                            resetPasswordStatus.setValue("Error: This email is not registered as a " + userType + ".");
                        }
                    } else {
                        // Error occurred while querying Firestore
                        resetPasswordStatus.setValue("Error checking email: " + Objects.requireNonNull(task.getException()).getMessage());
                        Log.e("LoginViewModel", Objects.requireNonNull(task.getException().getMessage()));
                    }
                });
    }

    // Sign in with Google for user (not owner)
    public void signInWithGoogle(Context context, SessionManager sessionManager, CheckBox rememberMeCheckBox) {
        GoogleAuthClient googleAuthClient = new GoogleAuthClient(context);

        // Handle Google Sign-In in a coroutine helper
        CoroutineHelper.Companion.signInWithGoogle(context, googleAuthClient, () -> {
            // On success, update login status to "user" (since this is for the user)
            loginStatus.setValue("user");

            NavigationHelper.navigateToMainActivity((AppCompatActivity) context);
            //ths
            sessionManager.saveAuthToken("googleuser", USER_TYPE_USER, rememberMeCheckBox.isChecked() );
        });
    }
}


