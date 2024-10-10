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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail,editTextPassword;
    MaterialButton buttonLogin,guestLogin;
    TextView textView;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){ // this will check if the user is already logged in or not, if yes then it will redirect user to main activity
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        textView = findViewById(R.id.jump_to_signup_page);
        buttonLogin = findViewById(R.id.login_btn);
        guestLogin = findViewById(R.id.guestSignIn_btn);
        progressBar = findViewById(R.id.login_progressBar);
        mAuth = FirebaseAuth.getInstance();
        //to sing up page
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //handles login in as a guest feature
        guestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //handles login feature
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);  // Show progress bar

                // Retrieve and trim the input values
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validate email
                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError(getString(R.string.enter_e_mail));
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }
                // Validate email format
                if (!isValidEmail(email)) {
                    editTextEmail.setError(getString(R.string.invalid_e_mail_format));
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Validate password
                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError(getString(R.string.enter_passwords));
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Attempt to sign in with FirebaseAuth
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);  // Hide progress bar after task completion
                                if (task.isSuccessful()) {
                                    // Sign in success, proceed to the main activity
                                    Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();  // Close the login activity
                                } else {
                                    // Sign in failed, handle error and show message
                                    if (task.getException() != null) {
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        switch (errorCode) {
                                            case "ERROR_USER_NOT_FOUND":
                                                // If email is not registered
                                                editTextEmail.setError(getString(R.string.user_not_found));
                                                editTextEmail.requestFocus();
                                                break;
                                            case "ERROR_INVALID_CREDENTIAL":
                                                // If the password is wrong
                                                editTextPassword.setError(getString(R.string.invaild_credentials));
                                                editTextPassword.requestFocus();
                                                break;
                                            default:
                                                // General error handling
                                                Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                }
                            }
                        });
            }
        });



    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
}