package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.ToastHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.LoginViewModelFactory;

import androidx.test.espresso.idling.CountingIdlingResource;


public class LoginActivity extends BaseActivity {

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin, googleButton;
    private TextView textViewSignUp, forgotPasswordTextView, titleTV;
    private ProgressBar progressBar;
    private MaterialCheckBox rememberMeCheckBox;
    private TextInputLayout login_password_layout,login_email_layout;
    private ImageView login_back_button;

    // Components
    private LoginViewModel loginViewModel;
    private SessionManager sessionManager;

    // User Type
    private String userType;

    private CountingIdlingResource idlingResource = new CountingIdlingResource("Network_Call");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements, components, and user type
        initializeUI();

        // Configure UI adjustments based on the user type (owner or user)
        configureBasedOnUserType();

        // Set up click listeners for UI interactions (login, sign up, forgot password, etc.)
        setupListeners();

        // Observe login status and reset password status to handle navigation and feedback
        observeLoginStatus();
    }

    /**
     * Initialize UI elements and components
     * <p>
     * This method sets up the UI elements such as email input, password input, buttons, progress bar, etc.
     * It also initializes the SessionManager and LoginViewModel to handle session and login logic.
     * </p>
     */
    private void initializeUI() {
        userType = getIntent().getStringExtra("userType");  // Get user type from intent
        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        buttonLogin = findViewById(R.id.login_btn);
        googleButton = findViewById(R.id.btnGoogleSignIn);
        textViewSignUp = findViewById(R.id.jump_to_signup_page);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.login_progressBar);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);
        titleTV = findViewById(R.id.titleTV);
        login_back_button = findViewById(R.id.login_back_button);

        login_password_layout = findViewById(R.id.login_password_layout);
        login_email_layout = findViewById(R.id.login_email_layout);

        sessionManager = SessionManager.getInstance(this);  // Initialize session manager

        AuthRepository authRepository = new AuthRepository();  // Initialize repository for authentication
        LoginViewModelFactory factory = new LoginViewModelFactory(authRepository);  // Create ViewModel factory
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);  // Initialize ViewModel
    }

    /**
     * Configure UI adjustments based on user type (user or owner)
     * <p>
     * This method adjusts the UI to show or hide elements based on the user type. For example, if the user is an owner,
     * it hides the Google sign-in button and other related elements.
     * </p>
     */
    private void configureBasedOnUserType() {
        LinearLayout divider = findViewById(R.id.or);  // Get the divider view

        // If the user is an owner, update the UI accordingly
        if (Objects.equals(userType, USER_TYPE_OWNER)) {
            titleTV.setText(R.string.owner);  // Set the title to "Owner"
            googleButton.setVisibility(View.GONE);  // Hide the Google sign-in button for owners
            divider.setVisibility(View.GONE);  // Hide the divider separating the login options
        }
    }

    /**
     * Set up click listeners for UI interactions.
     * <p>
     * This method sets up the listeners for button clicks (login, sign-up, forgot password, and Google sign-in),
     * triggering the appropriate actions such as handling login, navigating to the sign-up screen, or showing the forgot password dialog.
     * </p>
     */
    private void setupListeners() {
        login_back_button.setOnClickListener(v -> LoginHelper.navigateToFirst(this));
        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE); // Show the progress bar
            idlingResource.increment(); // Increment the counter
            new Handler().postDelayed(() -> {
                // Simulate network call
                LoginHelper.handleLogin(getApplicationContext(), editTextEmail, editTextPassword, progressBar, loginViewModel, userType, login_email_layout, login_password_layout);
                progressBar.setVisibility(View.GONE); // Hide the progress bar after delay
                idlingResource.decrement(); // Decrement the counter
            }, 2000); // 2-second delay
        });
        textViewSignUp.setOnClickListener(v -> LoginHelper.navigateToSignUp(this, userType));  // Sign-up text click handler
        googleButton.setOnClickListener(v -> loginViewModel.signInWithGoogle(this,sessionManager, rememberMeCheckBox));  // Google sign-in button click handler
        forgotPasswordTextView.setOnClickListener(v ->  LoginHelper.showForgotPasswordDialog(this, loginViewModel, userType));  // Forgot password text click handler
    }

    /**
     * Observe changes in login status and handle navigation.
     * <p>
     * This method observes the login status and reset password status from the ViewModel. Based on the status received,
     * it either proceeds with a successful login or shows an error message to the user.
     * </p>
     */
    private void observeLoginStatus() {
        // Observe login status and handle navigation or errors
        loginViewModel.getLoginStatus().observe(this, status -> {
            progressBar.setVisibility(View.GONE);  // Hide progress bar once login status is received

            // If login was successful (token received), handle successful login
            if (status.startsWith("token:")) {
                LoginHelper.handleSuccessfulLogin(status.substring(6), userType, rememberMeCheckBox, sessionManager, this);
            } else if (status.startsWith("error:")) {  // If an error occurred during login
                ToastHelper.showToast(this, status.substring(6));  // Show error message
            }
        });

        // Observe reset password status and show the corresponding message
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            ToastHelper.showToast(this, status);  // Show reset password status message
        });
    }
}
