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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.utility.LauncherUtils;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.LoginViewModelFactory;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.LoginViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.data.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin, googleButton;
    private TextView textViewSignUp, forgotPasswordTextView,titleTV;
    private ProgressBar progressBar;
    private MaterialCheckBox rememberMeCheckBox;

    // Shared Preferences for Remember Me
    private SharedPreferences sharedPreferences;

    // ViewModel
    private LoginViewModel loginViewModel;
    GoogleAuthClient googleAuthClient;
    private String loginAsType; // userType: "user" or "owner"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        AuthRepository authRepository = new AuthRepository();
        LoginViewModelFactory factory = new LoginViewModelFactory(authRepository);
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Observing the resetPasswordStatus to show feedback to the user
        loginViewModel.getResetPasswordStatus().observe(this, status -> {
            LauncherUtils.showToast(this,status);
        });

        initializeElements();

        // Set up UI actions
        setOnClickListeners();

        // Observe ViewModel login status
        observeLoginStatus();
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
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Get the "login_as" user type passed from previous activity
        loginAsType = getIntent().getStringExtra("login_as");
        if (Objects.equals(loginAsType, "owner")) {
            titleTV.setText("Owner");
            googleButton.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
    }
    private void setOnClickListeners() {
        // Login button
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (LauncherUtils.validateInputLogin(editTextEmail, editTextPassword)) {
                progressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(email, password, loginAsType);
            }
        });

        // Sign-Up navigation
        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("userType", loginAsType);
            startActivity(intent);
            finish();
        });

        // Google sign-in button (Assuming functionality exists in ViewModel)
        googleButton.setOnClickListener(v -> loginViewModel.signInWithGoogle(this));



        // Forgot password
        forgotPasswordTextView.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                loginViewModel.sendPasswordResetEmail(email);
            } else {
                LauncherUtils.showToast(this,getString(R.string.enter_your_email_first));
            }
        });
    }

    private void observeLoginStatus() {
        loginViewModel.getLoginStatus().observe(this, status -> {
            progressBar.setVisibility(View.GONE);
            if ("user".equals(status)) {

                LauncherUtils.navigateToMainActivity(this);
            } else if ("owner".equals(status)) {

                LauncherUtils.navigateToOwnerDashboard(this);
            } else if (status.startsWith("error:")) {
                LauncherUtils.showToast(this,status.substring(6));
            }
        });
    }
}
