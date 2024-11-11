/*Name: Kunal Dhiman, StudentID: N01540952, section number: RCB
  Name: Raghav Sharma, StudentID: N01537255, section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986, section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
*/
package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.CoroutineHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;
import ca.tech.sense.it.smart.indoor.parking.system.owner.OwnerActivity;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class LoginActivity extends BaseActivity {

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textView;

    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private MaterialCheckBox rememberMeCheckBox;
    private GoogleAuthClient googleAuthClient;
    private String loginAsType;  // Variable to store whether it's a user or owner login

    // Firebase Authentication instance
    FirebaseAuth mAuth;
    private MaterialButton googleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setUpWindowInsets();

        initializeUIElements();
        mAuth = FirebaseAuthSingleton.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Retrieve the "login_as" value passed from FirstActivity
        loginAsType = getIntent().getStringExtra("login_as");

        // Check if the user is already logged in
        checkIfUserLoggedIn();

        googleAuthClient = new GoogleAuthClient(this);

        // Set the OnClickListeners for buttons
        setOnClickListeners();
        forgetPassword();
    }



    @Override
    public void onStart() {
        super.onStart();
        checkIfUserLoggedIn();
        String token = sharedPreferences.getString("authToken", null);

        if (token != null) {
            // Token is present, so user is already logged in
            // You can validate the token with Firebase if needed, but for simplicity, we navigate directly
            navigateBasedOnRole();
        }
    }

    private void setUpWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    void initializeUIElements() {
        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        textView = findViewById(R.id.jump_to_signup_page);
        buttonLogin = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.login_progressBar);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);
        googleButton = findViewById(R.id.btnGoogleSignIn);
    }

    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateBasedOnRole();
        }
    }

    private void navigateBasedOnRole() {
        // Check if the login type is for an owner or user
        if (loginAsType != null) {
            if (loginAsType.equals("owner")) {
                navigateToOwnerDashboard();  // Owner-specific activity
            } else {
                navigateToMainActivity();  // User-specific activity
            }
        } else {
            navigateToMainActivity();  // Default to user if no type is passed
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToOwnerDashboard() {
        Intent intent = new Intent(getApplicationContext(), OwnerActivity.class); // Assuming this is the owner's dashboard
        startActivity(intent);
        finish();
    }

    private void setOnClickListeners() {
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            intent.putExtra("userType", loginAsType);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            hideKeyboard(v);
            performLogin();
        });

        googleButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        CoroutineHelper.Companion.signInWithGoogle(this, googleAuthClient);
        if (googleAuthClient.isSingedIn()){
            navigateBasedOnRole();
        }
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
                        // Get the Firebase ID token
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true) // Force refresh to get a new token
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String idToken = tokenTask.getResult().getToken();
                                            saveAuthToken(idToken);  // Save the token in SharedPreferences
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to get token", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                        // Proceed based on login type
                        if ("owner".equals(loginAsType)) {
                            checkIfUserIsOwner(email);
                        } else {
                            navigateToMainActivity();
                        }
                    } else {
                        handleLoginError(task);
                    }
                });
    }

    private void saveAuthToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", token);
        editor.apply();
    }


    private void checkIfUserIsOwner(String email) {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userID = currentUser.getUid();

            // Check if the user has an owner profile in the Firestore
            fireStore.collection("owners").document(userID).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                // User has an owner profile, navigate to the owner dashboard
                                navigateToOwnerDashboard();
                            } else {
                                // User does not have an owner profile, show error message
                                Toast.makeText(LoginActivity.this,
                                        "You need to sign up as an owner first.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Error checking owner profile.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    public void forgetPassword() {
        forgotPasswordTextView.setOnClickListener(v -> DialogUtil.showInputDialog(LoginActivity.this, "Enter Your Registered Email", "someone@mail.com", new DialogUtil.InputDialogCallback() {
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
                        Objects.requireNonNull(mAuth.getCurrentUser()).delete();
                        Toast.makeText(LoginActivity.this, R.string.this_email_is_not_registered, Toast.LENGTH_SHORT).show();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
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
                            Log.d("LoginActivity", R.string.error + Objects.requireNonNull(task.getException()).getMessage());
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