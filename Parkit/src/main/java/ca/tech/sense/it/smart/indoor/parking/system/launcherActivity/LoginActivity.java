/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class LoginActivity extends AppCompatActivity {

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textView, forgotPasswordTextView;
    private ProgressBar progressBar;

    private static final int RC_SIGN_IN = 1001; // Request code for sign-in
    private MaterialButton googleSignInButton; // Add this button in your layout

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // Google Sign-In Client
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setUpWindowInsets();

        initializeUIElements();
        mAuth = FirebaseAuth.getInstance();

        // Set up Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // From Firebase Console
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        checkIfUserLoggedIn();
        setOnClickListeners();
        forgetPassword();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkIfUserLoggedIn();
    }

    private void setUpWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
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
        progressBar = findViewById(R.id.login_progressBar);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        googleSignInButton = findViewById(R.id.google_sign_in_button); // Add this in your layout
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

        buttonLogin.setOnClickListener(v -> {
            hideKeyboard(v);
            performLogin();
        });

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
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
                    forgotPasswordTextView.setVisibility(View.VISIBLE);
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

    private void signInWithGoogle() {
        // Sign out any previous sessions
        mGoogleSignInClient.signOut();

        // Start the Google Sign-In intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Google Sign-In was successful, now authenticate with Firebase
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Google Sign-In failed, handle the exception
            Log.w("Google SignIn", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(LoginActivity.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Google SignIn", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Google Sign-In successful", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Firebase Auth", "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void forgetPassword() {
        forgotPasswordTextView.setOnClickListener(v -> DialogUtil.showInputDialog(LoginActivity.this, "Enter Your Registered Email", "someone@gmail.com", new DialogUtil.InputDialogCallback() {
            @Override
            public void onConfirm(String inputText) {
                if (TextUtils.isEmpty(inputText)) {
                    Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendPasswordResetEmail(inputText);
            }
            @Override
            public void onCancel() {
                //do nothing
            }
        }));
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.createUserWithEmailAndPassword(email, getString(R.string.dummypassword))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, getString(R.string.user_is_created_successfully));
                        Objects.requireNonNull(mAuth.getCurrentUser()).delete();
                        Toast.makeText(LoginActivity.this, R.string.this_email_is_not_registered, Toast.LENGTH_SHORT).show();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Log.d(TAG, getString(R.string.email_is_already_registered));
                            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, R.string.reset_link_sent_to_your_email, Toast.LENGTH_SHORT).show();
                                } else {
                                    if (task1.getException() instanceof FirebaseAuthInvalidUserException) {
                                        Toast.makeText(LoginActivity.this, R.string.this_email_is_not_registered, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.error) + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, R.string.error + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
