package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.signUp;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
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
        View scrollView = findViewById(R.id.signup);

        initializeUI();
        setOnClickListeners(userType,scrollView);

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

    private void setOnClickListeners(String userType, View scrollView) {
        // Navigate to Login screen
        jump_to_login.setOnClickListener(v -> SignUpHelper.navigateToLogin(this,userType));

        editTextPassword.setOnFocusChangeListener((v, hasFocus) -> SignUpHelper.scrollViewChangeBasedOnUi(hasFocus, editTextPassword, scrollView));

        // Handle Sign Up button click
        button.setOnClickListener(v -> SignUpHelper.handleSignUp(progressBar, editTextEmail, editTextPassword, editTextConfirmPassword, firstName, lastName, phone, checkBox, userType, mAuth, this));
    }
}
