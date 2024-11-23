package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.NavigationHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.ToastHelper;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.Constants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.Constants.USER_TYPE_USER;

public class FirstActivity extends BaseActivity {

    private MaterialButton signInAsUserButton, signInAsOwnerButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first);

        initViews();
        initSessionManager();
        handleRememberedUser();
        setButtonListeners();
        handleEdgeToEdgeLayout();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        signInAsUserButton = findViewById(R.id.firstSignInBut);
        signInAsOwnerButton = findViewById(R.id.firstSignInAsOwnerBut);
    }

    private void initSessionManager() {
        sessionManager = new SessionManager(this);
    }

    private void handleRememberedUser() {
        if (sessionManager.isRememberMe() && sessionManager.isUserLoggedIn()) {
            String userType = sessionManager.getUserType();
            navigateBasedOnUserType(userType, this);
        }
    }

    private void navigateBasedOnUserType(String userType, Context context) {
        switch (userType) {
            case USER_TYPE_OWNER:
                NavigationHelper.navigateToOwnerDashboard((AppCompatActivity) context);
                break;
            case USER_TYPE_USER:
                NavigationHelper.navigateToMainActivity((AppCompatActivity) context);
                break;
            default:
                ToastHelper.showToast(context, getString(R.string.unrecognized_user_type_please_log_in_again));
        }
    }

    private void setButtonListeners() {
        signInAsUserButton.setOnClickListener(v -> NavigationHelper.navigateToLoginFromFirst(USER_TYPE_USER, this));
        signInAsOwnerButton.setOnClickListener(v -> NavigationHelper.navigateToLoginFromFirst(USER_TYPE_OWNER,this));
    }

    private void handleEdgeToEdgeLayout() {
        View rootView = findViewById(R.id.firstmain);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}