package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.Manager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LauncherUtils;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.LoginViewModelFactory;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LoginViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    GoogleAuthClient googleAuthClient;

    // UI Elements for login screen
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin, googleButton;
    private TextView textViewSignUp;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    private MaterialCheckBox rememberMeCheckBox;

    // ViewModel to manage login logic
    private LoginViewModel loginViewModel;

    // User Type ("user" or "owner") passed from the previous activity
    private String userType;

    // Session Manager to handle user session details
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userType = getIntent().getStringExtra("userType");

        // Initialize the ViewModel for handling login logic
        AuthRepository authRepository = new AuthRepository();
        LoginViewModelFactory factory = new LoginViewModelFactory(authRepository);
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Initialize GoogleAuthClient
        googleAuthClient = new GoogleAuthClient(this);


        // Observing the resetPasswordStatus to show feedback to the user
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            // Display a toast message based on the reset password status
            LauncherUtils.showToast(this, status);
        });

        // Initialize UI elements and components
        initializeElements();

        // Set OnClickListeners for buttons and actions
        setOnClickListeners();

        // Observe login status to navigate user based on login success
        observeLoginStatus();

    }

    /**
     * Initializes the UI elements, SharedPreferences, ViewModel, and other components
     */
    private void initializeElements() {

        // Initialize UI elements from the layout
        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        buttonLogin = findViewById(R.id.login_btn);
        googleButton = findViewById(R.id.btnGoogleSignIn);
        textViewSignUp = findViewById(R.id.jump_to_signup_page);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.login_progressBar);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);
        TextView titleTV = findViewById(R.id.titleTV);


        // Hide or show Google sign-in for owners (assuming owner doesn't use Google sign-in)
        LinearLayout divider = findViewById(R.id.or);

        // Retrieve the userType ("user" or "owner") passed from the previous activity

        if (Objects.equals(userType, "owner")) {
            // Adjust the UI for owner (hide Google sign-in and title update)
            titleTV.setText(R.string.owner);
            googleButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }


    }

    /**
     * Sets the onClick listeners for login button, sign-up navigation, Google sign-in, and forgot password actions
     */
    private void setOnClickListeners() {
        // Handle login button click (verify inputs and trigger login in ViewModel)
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validate the input fields and trigger login
            if (LauncherUtils.validateInputLogin(editTextEmail, editTextPassword)) {
                progressBar.setVisibility(View.VISIBLE);  // Show progress bar while logging in
                loginViewModel.login(email, password, userType);
            }
        });

        // Navigate to Sign-Up Activity
        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("userType", userType);
            startActivity(intent);
            finish(); // Close the current login activity
        });

        // Handle Google sign-in button click (trigger Google sign-in in ViewModel)
        googleButton.setOnClickListener(v -> loginViewModel.signInWithGoogle(this));

        // Observe the loginStatus to navigate to the main activity on successful login
        loginViewModel.getLoginStatus().observe(this, status -> {
            if ("user".equals(status)) {
                // On successful user login, navigate to MainActivity
                LauncherUtils.navigateToMainActivity(this);
            } else {
                // If Google sign-in fails, show a failure message
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });

        // Handle forgot password click (show input dialog to enter email for password reset)
        forgotPasswordTextView.setOnClickListener(v -> DialogUtil.showInputDialog(this, getString(R.string.enter_your_registered_email), getString(R.string.someone_mail_com), new DialogUtil.InputDialogCallback() {
            @Override
            public void onConfirm(String inputText) {
                // Validate and send password reset email
                if (TextUtils.isEmpty(inputText)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.email_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                loginViewModel.sendPasswordResetEmail(inputText, userType);
            }

            @Override
            public void onCancel() {
                // Do nothing if canceled
            }
        }));

        // Observe the resetPasswordStatus LiveData and show feedback
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            // Display reset password status as a toast message
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Observe the login status and handle successful login or errors
     */
    private void observeLoginStatus() {
        loginViewModel.getLoginStatus().observe(this, status -> {
            // Hide the progress bar once login attempt is complete
            progressBar.setVisibility(View.GONE);

            // Initialize the SessionManager to manage session data
            sessionManager = new SessionManager(this);

            if (status.startsWith("token:")) {
                // Handle successful login (token received)
                String authToken = status.substring(6); // Extract the token
                sessionManager.saveAuthToken(authToken, userType, rememberMeCheckBox.isChecked());

                // Navigate to the appropriate activity based on the user type
                if (getString(R.string.user).equals(userType)) {
                    LauncherUtils.navigateToMainActivity(this);
                } else if (getString(R.string.owner).equals(userType)) {
                    LauncherUtils.navigateToOwnerDashboard(this);
                }
            } else if (status.startsWith("error:")) {
                // Handle login errors (show error message)
                LauncherUtils.showToast(this, status.substring(6));
            } else {
                // Handle unrecognized user type or first-time login
                if (getString(R.string.user).equals(userType)) {
                    if (sessionManager.isUserLoggedIn()) {
                        LauncherUtils.navigateToMainActivity(this);
                    } else {
                        LauncherUtils.showToast(this, getString(R.string.please_log_in_again));
                    }
                } else if (getString(R.string.owner).equals(userType)) {
                    if (sessionManager.isOwnerLoggedIn()) {
                        LauncherUtils.navigateToOwnerDashboard(this);
                    } else {
                        LauncherUtils.showToast(this, getString(R.string.please_log_in_again));
                    }
                } else {
                    // Unrecognized user type (show error)
                    LauncherUtils.showToast(this,  getString(R.string.unrecognized_user_type_please_log_in_again));
                }
            }
        });
    }
}
