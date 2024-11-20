package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.LauncherUtils;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;

public class SignUpActivity extends BaseActivity {

    // Variables
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, firstName, lastName, phone;
    private MaterialButton button;
    private TextView textView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fireStore;
    private CheckBox checkBox;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        initializeUI();
        setOnClickListeners(getIntent().getStringExtra("userType"));
    }

    private void initializeUI() {
        // Initialize UI components
        editTextEmail = findViewById(R.id.signup_editTextEmail);
        editTextPassword = findViewById(R.id.signup_editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        textView = findViewById(R.id.jump_to_login);
        button = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.signup_progressBar);
        checkBox = findViewById(R.id.checkBoxTerms);
        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        phone = findViewById(R.id.signup_phoneNumber);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
    }

    private void setOnClickListeners(String userType) {
        // Navigate to Login screen
        textView.setOnClickListener(v -> {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });

        // Handle Sign Up button click
        button.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            // Get user input
            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();
            String phoneNumber = phone.getText().toString().trim();

            // Input validation
            if (!validateInput(fName, lName, email, password, confirmPassword, phoneNumber)) {
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Sign up the user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            Log.d("SignUpActivity", "Firebase Auth success, UID: " + userID);

                            // Handle User or Owner data storage
                            if ("owner".equals(userType)) {
                                saveOwnerToFirestore(fName, lName, email, phoneNumber);
                            } else {
                                saveUserToFirestore(fName, lName, email, phoneNumber);
                            }

                        } else {
                            Log.e("SignUpActivity", "Firebase Auth failed: " + Objects.requireNonNull(task.getException()).getMessage());
                            LauncherUtils.showToast(this, getString(R.string.authentication_failed));
                        }
                    });
        });
    }

    private boolean validateInput(String fName, String lName, String email, String password, String confirmPassword, String phoneNumber) {
        if (TextUtils.isEmpty(fName)) {
            firstName.setError("Please enter your first name");
            return false;
        }
        if (TextUtils.isEmpty(lName)) {
            lastName.setError("Please enter your last name");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.enter_e_mail));
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phone.setError("Enter phone number");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.enter_passwords));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return false;
        }
        if (!checkBox.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveOwnerToFirestore(String fName, String lName, String email, String phoneNumber) {
        Owner localOwner = new Owner(userID, fName, lName, email, phoneNumber, null);
        fireStore.collection("owners").document(userID).set(localOwner)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignUpActivity", "Owner profile created successfully: " + userID);
                    LauncherUtils.navigateToOwnerDashboard(this);
                    finish();
                })
                .addOnFailureListener(e -> Log.e("SignUpActivity", "Error saving owner data: " + e.getMessage()));
    }

    private void saveUserToFirestore(String fName, String lName, String email, String phoneNumber) {
        User localUser = new User(userID, fName, lName, email, phoneNumber, null);
        fireStore.collection("users").document(userID).set(localUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignUpActivity", "User profile created successfully: " + userID);
                    LauncherUtils.navigateToMainActivity(this);
                    finish();
                })
                .addOnFailureListener(e -> Log.e("SignUpActivity", "Error saving user data: " + e.getMessage()));
    }
}
