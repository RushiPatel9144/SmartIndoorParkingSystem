package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.NavigationHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;

/**
 * FirstActivity serves as the entry point after the splash screen and provides
 * options for the user to either sign in as a regular user or as an owner.
 * This activity handles user session checks and navigates accordingly
 * if a "Remember Me" session is active.
 */
public class FirstActivity extends BaseActivity {

    // UI elements
    private MaterialButton signInAsUserButton, signInAsOwnerButton;

    // Session manager for handling user sessions
    private SessionManager sessionManager;

    /**
     * Called when the activity is first created.
     * Initializes the UI, sets up session handling, and configures navigation behavior.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge layout for immersive UI
        setContentView(R.layout.activity_first);  // Set the layout for the activity

        // Initialize views and components
        initViews();
        initSessionManager();

        // Check if a remembered user session exists and handle navigation
        handleRememberedUser();

        // Set listeners for sign-in buttons
        setButtonListeners();

        // Adjust UI layout for edge-to-edge experience
        handleEdgeToEdgeLayout();
    }

    /**
     * Initializes UI elements in the activity.
     */
    @SuppressLint("WrongViewCast")
    private void initViews() {
        // Assign buttons for user and owner sign-in
        signInAsUserButton = findViewById(R.id.firstSignInBut);
        signInAsOwnerButton = findViewById(R.id.firstSignInAsOwnerBut);
    }

    /**
     * Initializes the session manager for managing user session data.
     */
    private void initSessionManager() {
        sessionManager = new SessionManager(this);
    }

    /**
     * Checks if the "Remember Me" feature is active.
     * If so, it determines the user type and navigates to the corresponding screen.
     */
    private void handleRememberedUser() {
        // Check if "Remember Me" is enabled and a user or owner is logged in
        if (sessionManager.isRememberMe() && (sessionManager.isUserLoggedIn() || sessionManager.isOwnerLoggedIn())) {
            // Retrieve the user type and navigate accordingly
            String userType = sessionManager.getUserType();
            NavigationHelper.navigateBasedOnUserType(userType, this);
        }
    }


    /**
     * Sets click listeners for the sign-in buttons.
     */
    private void setButtonListeners() {
        // Navigate to the login screen as a regular user
        signInAsUserButton.setOnClickListener(v -> NavigationHelper.navigateToLoginFromFirst(USER_TYPE_USER, this));

        // Navigate to the login screen as an owner
        signInAsOwnerButton.setOnClickListener(v -> NavigationHelper.navigateToLoginFromFirst(USER_TYPE_OWNER, this));
    }

    /**
     * Adjusts the layout to accommodate edge-to-edge system bars for a more immersive experience.
     */
    private void handleEdgeToEdgeLayout() {
        // Find the root view of the activity
        View rootView = findViewById(R.id.firstmain);
        if (rootView != null) {
            // Adjust padding based on system bar insets
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}
