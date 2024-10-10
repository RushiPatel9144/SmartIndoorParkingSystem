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
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class SignUpActivity extends AppCompatActivity {



    EditText editTextEmail,editTextPassword,editTextConfirmPassword,firstName,lastName;
    MaterialButton button;
    TextView textView;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    CheckBox checkBox;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){ // this will check if the user is already logged in or not, if yes then it will redirect user to main activity
            Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
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
        //Initialize all the fields
        editTextEmail = findViewById(R.id.signup_editTextEmail);
        editTextPassword = findViewById(R.id.signup_editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        textView = findViewById(R.id.jump_to_login);
        button = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.signup_progressBar);
        mAuth = FirebaseAuth.getInstance();
        checkBox = findViewById(R.id.checkBoxTerms);
        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);  // Show progress bar
                String fName = firstName.getText().toString().trim();
                String lName = lastName.getText().toString().trim();
                String email = String.valueOf(editTextEmail.getText());
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                
                // Validate first name
                if (TextUtils.isEmpty(fName)) {
                    firstName.setError("Please enter your first name");
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Validate last name
                if (TextUtils.isEmpty(lName)) {
                    lastName.setError("Please enter your last name");
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }
                // Validate email
                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError(getString(R.string.enter_e_mail));
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Validate password
                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError(getString(R.string.enter_passwords));
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    editTextConfirmPassword.setError("Passwords do not match");
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Check if checkbox is checked
                if (!checkBox.isChecked()) {
                    Toast.makeText(SignUpActivity.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // If all validations pass, create the user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);  // Hide progress bar after task completion
                                if (task.isSuccessful()) {
                                    // Sign up successful
                                    Toast.makeText(SignUpActivity.this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();  // Close the sign-up activity
                                } else {
                                    // Sign up failed
                                    Toast.makeText(SignUpActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}