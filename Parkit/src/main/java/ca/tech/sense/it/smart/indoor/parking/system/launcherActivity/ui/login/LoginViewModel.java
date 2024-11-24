package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.Constants.USER_TYPE_USER;

import android.content.Context;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.CoroutineHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.NavigationHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;


public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;  // The repository that handles authentication and user data
    private final MutableLiveData<String> loginStatus = new MutableLiveData<>();  // LiveData to observe login status
    private final MutableLiveData<String> resetPasswordStatus = new MutableLiveData<>();  // LiveData to observe password reset status

    // Constructor initializes the AuthRepository
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // Getters for LiveData objects to observe login and reset password status
    public LiveData<String> getLoginStatus() {
        return loginStatus;
    }

    public LiveData<String> getResetPasswordStatus() {
        return resetPasswordStatus;
    }

    /**
     * Attempts to log in the user with email and password.
     * It checks if the user is authenticated and then retrieves a token.
     */
    public void login(String email, String password, String userType) {
        authRepository.login(email, password)  // Call to AuthRepository to log in with email and password
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();  // If login is successful, get FirebaseUser
                    if (user != null) {
                        fetchUserToken(user, userType);  // Retrieve the token if the user is not null
                    } else {
                        loginStatus.setValue("error:Authentication failed. User is null.");  // Handle failure case
                    }
                })
                .addOnFailureListener(e -> loginStatus.setValue("error:" + e.getMessage()));  // Handle failure to log in
    }

    /**
     * Fetches the Firebase authentication token for the user after login.
     * The token is then used to validate the user type (owner or user).
     */
    private void fetchUserToken(FirebaseUser user, String userType) {
        user.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String authToken = task.getResult().getToken();  // Retrieve the auth token from Firebase
                // Validate the user type and update the login status
                LoginUserTypeValidator.validateUserType(user.getUid(), userType, authToken, loginStatus, authRepository);
            } else {
                loginStatus.setValue("error:Failed to retrieve token: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    /**
     * Sends a password reset email to the user if the email is registered.
     * It checks whether the email is registered as a "user" or "owner" and proceeds accordingly.
     */
    public void sendPasswordResetEmail(String email, String userType) {
        String collection = "user".equals(userType) ? "users" : "owners";  // Determine the collection based on user type

        // Check if the email is registered in the appropriate collection (users or owners)
        authRepository.isEmailRegisteredInCollection(email, collection)
                .addOnSuccessListener(isRegistered -> {
                    if (isRegistered) {
                        // If email is registered, send a password reset email
                        authRepository.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> {
                                    resetPasswordStatus.setValue("Password reset email sent successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    resetPasswordStatus.setValue("Error sending password reset email: " + e.getMessage());
                                });
                    } else {
                        resetPasswordStatus.setValue("Error: This email is not registered as a " + userType + ".");
                    }
                })
                .addOnFailureListener(e -> resetPasswordStatus.setValue("Error checking email: " + e.getMessage()));  // Handle error in checking email registration
    }

    /**
     * Initiates Google sign-in process using GoogleAuthClient and CoroutineHelper.
     * Upon success, the user is navigated to the main activity.
     */
    public void signInWithGoogle(Context context, SessionManager sessionManager, CheckBox rememberMeCheckBox) {
        GoogleAuthClient googleAuthClient = new GoogleAuthClient(context);  // Create GoogleAuthClient instance

        // Use CoroutineHelper to handle the Google sign-in asynchronously
        CoroutineHelper.Companion.signInWithGoogle(context, googleAuthClient, () -> {
            loginStatus.setValue("user");  // Update login status on successful sign-in
            NavigationHelper.navigateToMainActivity((AppCompatActivity) context);  // Navigate to main activity
            sessionManager.saveAuthToken("googleuser", USER_TYPE_USER, rememberMeCheckBox.isChecked());  // Save the session token and remember me status
        });
    }
}