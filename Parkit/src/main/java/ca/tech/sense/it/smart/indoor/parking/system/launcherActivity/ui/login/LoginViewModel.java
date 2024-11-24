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
                        fetchUserToken(user, userType);
                    } else {
                        loginStatus.setValue("error:Authentication failed. User is null.");
                    }
                })
                .addOnFailureListener(e -> loginStatus.setValue("error:" + e.getMessage()));
    }

    private void fetchUserToken(FirebaseUser user, String userType) {
        user.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String authToken = task.getResult().getToken();
                LoginUserTypeValidator.validateUserType(user.getUid(), userType, authToken, loginStatus, authRepository);
            } else {
                loginStatus.setValue("error:Failed to retrieve token: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }



    public void sendPasswordResetEmail(String email, String userType) {
        String collection = "user".equals(userType) ? "users" : "owners";

        authRepository.isEmailRegisteredInCollection(email, collection)
                .addOnSuccessListener(isRegistered -> {
                    if (isRegistered) {
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
                .addOnFailureListener(e -> resetPasswordStatus.setValue("Error checking email: " + e.getMessage()));
    }

    public void signInWithGoogle(Context context, SessionManager sessionManager, CheckBox rememberMeCheckBox) {
        GoogleAuthClient googleAuthClient = new GoogleAuthClient(context);

        CoroutineHelper.Companion.signInWithGoogle(context, googleAuthClient, () -> {
            loginStatus.setValue("user");
            NavigationHelper.navigateToMainActivity((AppCompatActivity) context);
            sessionManager.saveAuthToken("googleuser", USER_TYPE_USER, rememberMeCheckBox.isChecked());
        });
    }
}
