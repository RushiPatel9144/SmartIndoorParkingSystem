package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.content.Intent;
import android.icu.text.CaseMap;
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
    private TextView jump_to_login,titleTV;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fireStore;
    private CheckBox checkBox;
    private String userID,userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_sign_up);

        userType =  getIntent().getStringExtra("userType");

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
        titleTV = findViewById(R.id.signup_title_tv);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

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

    private boolean validateInput(String fName, String lName, String email, String password, String confirmPassword, String phoneNumber) {
        if (TextUtils.isEmpty(fName)) {
            firstName.setError(getString(R.string.please_enter_your_first_name));
            return false;
        }
        if (TextUtils.isEmpty(lName)) {
            lastName.setError(getString(R.string.please_enter_your_last_name));
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.enter_e_mail));
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phone.setError(getString(R.string.enter_phone_number));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.enter_passwords));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            return false;
        }
        if (!checkBox.isChecked()) {
            Toast.makeText(this, getString(R.string.sign_please_accept_the_terms_and_conditions), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
