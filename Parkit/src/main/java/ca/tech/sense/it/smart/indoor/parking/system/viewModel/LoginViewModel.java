package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.CoroutineHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.data.AuthRepository;

public class    LoginViewModel extends ViewModel {
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
                                    checkIfOwner(user.getUid(), authToken); // Pass the token
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
    public void sendPasswordResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    resetPasswordStatus.setValue("Password reset email sent successfully.");
                })
                .addOnFailureListener(e -> {
                    resetPasswordStatus.setValue("Error sending password reset email: " + e.getMessage());
                });
    }

    // Sign in with Google for user (not owner)
    public void signInWithGoogle(Context context) {
        GoogleAuthClient googleAuthClient = new GoogleAuthClient(context);

        // Handle Google Sign-In in a coroutine helper
        CoroutineHelper.Companion.signInWithGoogle(context, googleAuthClient, () -> {
            // On success, update login status to "user" (since this is for the user)
            loginStatus.setValue("user");


        });
    }
}


