/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;

public class SignUpActivity extends BaseActivity {

    EditText editTextEmail, editTextPassword, editTextConfirmPassword, firstName, lastName, phone;
    MaterialButton button;
    TextView textView;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    CheckBox checkBox;
    FirebaseFirestore fireStore;
    String userID;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        editTextEmail = findViewById(R.id.signup_editTextEmail);
        editTextPassword = findViewById(R.id.signup_editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        textView = findViewById(R.id.jump_to_login);
        button = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.signup_progressBar);
        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        checkBox = findViewById(R.id.checkBoxTerms);
        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        phone = findViewById(R.id.signup_phoneNumber);

        // Navigate to Login screen
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle the Sign Up button click
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
            if (TextUtils.isEmpty(fName)) {
                firstName.setError("Please enter your first name");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(lName)) {
                lastName.setError("Please enter your last name");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(email)) {
                editTextEmail.setError(getString(R.string.enter_e_mail));
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(phoneNumber)) {
                phone.setError("Enter phone number");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                editTextPassword.setError(getString(R.string.enter_passwords));
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                editTextConfirmPassword.setError("Passwords do not match");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (!checkBox.isChecked()) {
                Toast.makeText(SignUpActivity.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Create user in Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Sign up successful
                            Toast.makeText(SignUpActivity.this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();

                            // Create User object
                            User localUser = new User(userID, fName, lName, email, phoneNumber, null); // Profile photo URL is null for now

                            // Store user data in Firestore
                            DocumentReference documentReference = fireStore.collection("users").document(userID);
                            documentReference.set(localUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("TAG", "User profile is created for " + userID);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("TAG", "Error saving user data: " + e.getMessage());
                                    });

                            // Navigate to MainActivity
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign up failed
                            Toast.makeText(SignUpActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
