package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.LauncherUtils;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;

public class SignUpActivity extends BaseActivity {

    // Variables
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, firstName, lastName, phone;
    private MaterialButton button;
    private TextView jump_to_login;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private CheckBox checkBox;
    private String userID,userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);



        initializeUI();
        setOnClickListeners(userType);

    }

    private void initializeUI() {
        // Initialize UI components
        editTextEmail = findViewById(R.id.signup_editTextEmail);
        editTextPassword = findViewById(R.id.signup_editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        jump_to_login = findViewById(R.id.jump_to_login);
        button = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.signup_progressBar);
        checkBox = findViewById(R.id.checkBoxTerms);
        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        phone = findViewById(R.id.signup_phoneNumber);
        TextView titleTV = findViewById(R.id.signup_title_tv);

        mAuth = FirebaseAuth.getInstance();
        userType =  getIntent().getStringExtra("userType");
        if (Objects.equals(userType, getString(R.string.small_owner))){
            titleTV.setText(getString(R.string.owner));
        }


    }

    private void setOnClickListeners(String userType) {
        // Navigate to Login screen
        jump_to_login.setOnClickListener(v -> {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            loginIntent.putExtra("userType", userType);
            startActivity(loginIntent);
            finish();
        });

        editTextPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                View scrollView = findViewById(R.id.signup);
                scrollView.scrollTo(0, editTextPassword.getBottom());
            }
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
            if (!LauncherUtils.validateInput(SignUpActivity.this, firstName, lastName, editTextEmail, phone, editTextPassword, editTextConfirmPassword, checkBox, fName, lName, email, password, confirmPassword, phoneNumber)) {
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
                                LauncherUtils.saveOwnerToFirestore(this, userID, fName, lName, email, phoneNumber);
                            } else {
                                LauncherUtils.saveUserToFirestore(this, userID, fName, lName, email, phoneNumber);
                            }

                        } else {
                            Log.e("SignUpActivity", "Firebase Auth failed: " + Objects.requireNonNull(task.getException()).getMessage());
                            LauncherUtils.showToast(this, getString(R.string.authentication_failed));
                        }
                    });
        });
    }

}
