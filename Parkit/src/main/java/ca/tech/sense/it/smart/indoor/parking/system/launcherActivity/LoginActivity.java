package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.tech.sense.it.smart.indoor.parking.system.MainActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail,editTextPassword;
    MaterialButton buttonLogin,guestLogin;
    TextView textView;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){ // this will check if the user is already logged in or not, if yes then it will redirect user to main activity
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.login_email_editext);
        editTextPassword = findViewById(R.id.login_password_editext);
        textView = findViewById(R.id.jump_to_signup_page);
        buttonLogin = findViewById(R.id.login_btn);
        guestLogin = findViewById(R.id.guestSignIn_btn);
        progressBar = findViewById(R.id.login_progressBar);
        mAuth = FirebaseAuth.getInstance();
        //to sing up page
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        guestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email,password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                //when email is empty
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this, R.string.enter_email, Toast.LENGTH_SHORT).show();
                    return  ;
                }
                //when password is empty - remainder change this to alert in post(RushiPatel)
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, (R.string.enter_password), Toast.LENGTH_SHORT).show();
                }
                //checks for user in firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(LoginActivity.this, (R.string.login_successful),
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, (R.string.authentication_failed),
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

    }
}