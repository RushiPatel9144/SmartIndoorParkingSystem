package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.signUp;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.TermsConditionsBottomSheet;

public class SignUpActivity extends AppCompatActivity implements TermsConditionsBottomSheet.TermsDialogListener {

    // Variables
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, firstName, lastName, phone;
    private MaterialButton button;
    private TextView jump_to_login;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private CheckBox checkBox;
    private ImageView signup_back_button;
    private String userID, userType;
    private TextInputLayout signup_first_name_layout, signup_last_name_layout, signup_phone_number_layout, signup_email_layout, signup_password_layout, signup_confirm_password_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        View scrollView = findViewById(R.id.signup);

        initializeUI();
        setOnClickListeners(userType, scrollView);
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
        signup_back_button = findViewById(R.id.signup_back_button);

        signup_first_name_layout = findViewById(R.id.signup_first_name_layout);
        signup_last_name_layout = findViewById(R.id.signup_last_name_layout);
        signup_phone_number_layout = findViewById(R.id.signup_phone_number_layout);
        signup_email_layout = findViewById(R.id.signup_email_layout);
        signup_password_layout = findViewById(R.id.signup_password_layout);
        signup_confirm_password_layout = findViewById(R.id.signup_confirm_password_layout);

        TextView titleTV = findViewById(R.id.signup_title_tv);

        mAuth = FirebaseAuth.getInstance();
        userType = getIntent().getStringExtra("userType");
        if (Objects.equals(userType, getString(R.string.small_owner))) {
            titleTV.setText(getString(R.string.owner));
        }
    }

    private void setOnClickListeners(String userType, View scrollView) {
        // Show Terms and Conditions dialog when checkbox clicked
        checkBox.setOnClickListener(v -> {
            if (!checkBox.isChecked()) {
                showTermsDialog();
            }
        });

        // Navigate to Login screen
        jump_to_login.setOnClickListener(v -> SignUpHelper.navigateToLogin(this, userType));
        signup_back_button.setOnClickListener(v -> SignUpHelper.navigateToLogin(this, userType));

        editTextPassword.setOnFocusChangeListener((v, hasFocus) -> SignUpHelper.scrollViewChangeBasedOnUi(hasFocus, editTextPassword, editTextConfirmPassword, scrollView));
        // Handle Sign Up button click
        button.setOnClickListener(v -> SignUpHelper.handleSignUp(progressBar, editTextEmail, editTextPassword, editTextConfirmPassword, firstName, lastName, phone, checkBox, userType, mAuth, this, signup_email_layout, signup_password_layout, signup_confirm_password_layout, signup_phone_number_layout, signup_first_name_layout, signup_last_name_layout));
    }

    private void showTermsDialog() {
        TermsConditionsBottomSheet dialog = new TermsConditionsBottomSheet();
        dialog.show(getSupportFragmentManager(), "TermsConditionsDialog");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onTermsAccepted() {
        checkBox.setChecked(true);
    }
}
