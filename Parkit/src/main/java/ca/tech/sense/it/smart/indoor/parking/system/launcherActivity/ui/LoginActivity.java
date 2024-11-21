package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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
import ca.tech.sense.it.smart.indoor.parking.system.utility.LauncherUtils;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.LoginViewModelFactory;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.LoginViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    GoogleAuthClient googleAuthClient;

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin, googleButton;
    private TextView textViewSignUp, forgotPasswordTextView, titleTV;
    private ProgressBar progressBar;
    private MaterialCheckBox rememberMeCheckBox;
    // Shared Preferences for Remember Me
    private SharedPreferences sharedPreferences;
    // ViewModel
    private LoginViewModel loginViewModel;
    private String userType; // userType: "user" or "owner"
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeElements();
        setOnClickListeners();  // Set up UI actions
        observeLoginStatus();// Observe ViewModel login status
        observeResetPasswordStatus();
    }


    private void initializeElements() {

        // Initialize UI elements
        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        buttonLogin = findViewById(R.id.login_btn);
        googleButton = findViewById(R.id.btnGoogleSignIn);
        textViewSignUp = findViewById(R.id.jump_to_signup_page);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.login_progressBar);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);
        titleTV = findViewById(R.id.titleTV);
        googleAuthClient = new GoogleAuthClient(this);
        LinearLayout divider = findViewById(R.id.or);

        // SharedPreferences for Remember Me
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);

        // Get the "login_as" user type passed from previous activity
        userType = getIntent().getStringExtra("userType");
        if (Objects.equals(userType, "owner")) {
            titleTV.setText(R.string.owner);
            googleButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        // Initialize ViewModel
        AuthRepository authRepository = new AuthRepository();
        LoginViewModelFactory factory = new LoginViewModelFactory(authRepository);
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);


        // Observing the resetPasswordStatus to show feedback to the user
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            LauncherUtils.showToast(this, status);
        });
    }

    private void setOnClickListeners() {
        // Login button
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (LauncherUtils.validateInputLogin(editTextEmail, editTextPassword)) {
                progressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(email, password, userType);
            }
        });

        // Sign-Up navigation
        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("userType", userType);
            startActivity(intent);
            finish();
        });

        // Google sign-in button (Assuming functionality exists in ViewModel)
        googleButton.setOnClickListener(v -> loginViewModel.signInWithGoogle(this));


        forgotPasswordTextView.setOnClickListener(v -> {
            DialogUtil.showInputDialog(this, "Enter Your Registered Email", "someone@mail.com", new DialogUtil.InputDialogCallback() {
                @Override
                public void onConfirm(String inputText) {
                    if (TextUtils.isEmpty(inputText)) {
                        Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loginViewModel.sendPasswordResetEmail(inputText,userType);
                }

                @Override
                public void onCancel() {
                    // Do nothing
                }
            });
        });

        // Observe the resetPasswordStatus LiveData
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            if (status.startsWith("Error")) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });



    }
    private void observeResetPasswordStatus() {
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            LauncherUtils.showToast(this, status);
        });
    }
    private void observeLoginStatus() {
        loginViewModel.getLoginStatus().observe(this, status -> {
            progressBar.setVisibility(View.GONE);

            // Initialize the SessionManager
            SessionManager sessionManager = new SessionManager(this);

            if (status.startsWith("token:")) {
                // Handle successful login with token
                String authToken = status.substring(6); // Extract token
                sessionManager.saveAuthToken(authToken, userType, rememberMeCheckBox.isChecked());

                // Navigate based on user type
                if ("user".equals(userType)) {
                    LauncherUtils.navigateToMainActivity(this);
                } else if ("owner".equals(userType)) {
                    LauncherUtils.navigateToOwnerDashboard(this);
                }
            } else if (status.startsWith("error:")) {
                // Handle login error
                LauncherUtils.showToast(this, status.substring(6));
            } else {
                // Handle invalid status or first-time login
                if ("user".equals(userType)) {
                    if (sessionManager.isUserLoggedIn()) {
                        LauncherUtils.navigateToMainActivity(this);
                    } else {
                        LauncherUtils.showToast(this, "Please log in again.");
                    }
                } else if ("owner".equals(userType)) {
                    if (sessionManager.isOwnerLoggedIn()) {
                        LauncherUtils.navigateToOwnerDashboard(this);
                    } else {
                        LauncherUtils.showToast(this, "Please log in again.");
                    }
                } else {
                    // Unrecognized user type
                    LauncherUtils.showToast(this, "Unrecognized user type. Please log in again.");
                }
            }
        });
    }
}
