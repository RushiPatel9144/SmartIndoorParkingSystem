package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.userLogin;

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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner; // Import Owner class
import ca.tech.sense.it.smart.indoor.parking.system.owner.OwnerActivity;

public class UserSignUpActivity extends AppCompatActivity {

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

        // Get the user type passed from the login screen
        Intent intent = getIntent();
        String userType = intent.getStringExtra("userType");




        // Navigate to Login screen
        textView.setOnClickListener(v -> {
            Intent loginIntent = new Intent(getApplicationContext(), UserLoginActivity.class);
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

            // Input validation (add checks as needed)
            if (!validateInput(fName, lName, email, password, confirmPassword, phoneNumber)) {
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Sign up the user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(UserSignUpActivity.this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();

                            // Create either a User or Owner object based on the user type

                            if ("owner".equals(userType)) {
                                Owner localOwner = new Owner(userID, fName, lName, email, phoneNumber, null);
                                fireStore.collection("owners").document(userID).set(localOwner)
                                        .addOnSuccessListener(aVoid -> Log.d("TAG", "Owner profile is created for " + userID))
                                        .addOnFailureListener(e -> Log.d("TAG", "Error saving owner data: " + e.getMessage()));
                                Intent mainIntent = new Intent(getApplicationContext(), OwnerActivity.class);
                                startActivity(mainIntent);
                                finish();
                            } else {
                                User localUser = new User(userID, fName, lName, email, phoneNumber, null);
                                fireStore.collection("users").document(userID).set(localUser)
                                        .addOnSuccessListener(aVoid -> Log.d("TAG", "User profile is created for " + userID))
                                        .addOnFailureListener(e -> Log.d("TAG", "Error saving user data: " + e.getMessage()));
                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }

                            // Navigate to MainActivity

                        } else {
                            Toast.makeText(UserSignUpActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(UserSignUpActivity.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
