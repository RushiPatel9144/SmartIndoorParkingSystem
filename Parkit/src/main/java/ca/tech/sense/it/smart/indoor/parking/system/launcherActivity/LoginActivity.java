package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AuthUtils;

public class LoginActivity extends AppCompatActivity {

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin, guestLogin;
    private TextView textView, forgotPasswordTextView;
    private ProgressBar progressBar;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setUpWindowInsets();

        initializeUIElements();
        mAuth = FirebaseAuth.getInstance();
        checkIfUserLoggedIn();
        setOnClickListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkIfUserLoggedIn();
    }

    private void setUpWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeUIElements() {
        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        textView = findViewById(R.id.jump_to_signup_page);
        buttonLogin = findViewById(R.id.login_btn);
        guestLogin = findViewById(R.id.guestSignIn_btn);
        progressBar = findViewById(R.id.login_progressBar);
        forgotPasswordTextView = findViewById(R.id.forgot_password);

    }

    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setOnClickListeners() {
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        guestLogin.setOnClickListener(v -> navigateToMainActivity());

        buttonLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        progressBar.setVisibility(View.VISIBLE);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        handleLoginError(task);
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.enter_e_mail));
            return false;
        }
        if (!isValidEmail(email)) {
            editTextEmail.setError(getString(R.string.invalid_e_mail_format));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.enter_passwords));
            return false;
        }
        return true;
    }

    private void handleLoginError(@NonNull Task<AuthResult> task) {
        if (task.getException() != null) {
            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
            switch (errorCode) {
                case "ERROR_USER_NOT_FOUND":
                    editTextEmail.setError(getString(R.string.user_not_found));
                    editTextEmail.requestFocus();
                    break;
                case "ERROR_INVALID_CREDENTIAL":
                    editTextPassword.setError(getString(R.string.invaild_credentials));
                    editTextPassword.requestFocus();
                    break;
                default:
                    Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
}
