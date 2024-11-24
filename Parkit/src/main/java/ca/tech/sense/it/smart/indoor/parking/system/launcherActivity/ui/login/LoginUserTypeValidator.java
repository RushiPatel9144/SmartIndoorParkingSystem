package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login;

import androidx.lifecycle.MutableLiveData;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;

public class LoginUserTypeValidator {

    // Public method to validate user type and trigger appropriate validation
    public static void validateUserType(String userId, String userType, String authToken, MutableLiveData<String> loginStatus, AuthRepository authRepository) {
        if ("owner".equals(userType)) {
            validateOwner(userId, authToken, authRepository, loginStatus);
        } else if ("user".equals(userType)) {
            validateUser(userId, authToken, authRepository, loginStatus);
        } else {
            loginStatus.setValue("error:Invalid user type.");
        }
    }

    // Validate if the user is an owner
    private static void validateOwner(String userId, String authToken, AuthRepository authRepository, MutableLiveData<String> loginStatus) {
        authRepository.checkOwner(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loginStatus.setValue("token:" + authToken); // Owner login
                    } else {
                        loginStatus.setValue("error:Not an owner. Please sign up.");
                    }
                })
                .addOnFailureListener(e -> loginStatus.setValue("error:Failed to check owner: " + e.getMessage()));
    }

    // Validate if the user is a regular user
    private static void validateUser(String userId, String authToken, AuthRepository authRepository, MutableLiveData<String> loginStatus) {
        authRepository.checkUser(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loginStatus.setValue("token:" + authToken); // User login
                    } else {
                        loginStatus.setValue("error:Not a user. Please sign up.");
                    }
                })
                .addOnFailureListener(e -> loginStatus.setValue("error:Failed to check user: " + e.getMessage()));
    }
}
