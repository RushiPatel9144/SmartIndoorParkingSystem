package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import ca.tech.sense.it.smart.indoor.parking.system.manager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LauncherUtils;

public class FirstActivity extends AppCompatActivity {
    private MaterialButton signInAsUserButton, signInAsOwnerButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first);

        signInAsUserButton = findViewById(R.id.firstSignInBut);
        signInAsOwnerButton = findViewById(R.id.firstSignInAsOwnerBut);

        // Initialize SessionManager
        SessionManager sessionManager = new SessionManager(this);

        // Set up button listeners for user and owner sign-in
        signInAsUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("userType", "user");
            startActivity(intent);
        });

        signInAsOwnerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("userType", "owner");
            startActivity(intent);
        });

        if(sessionManager.isRememberMe()){
            // Check if a user is already logged in
            if (sessionManager.isUserLoggedIn()) {
                String userType = sessionManager.getUserType();
                if ("owner".equals(userType)) {
                    LauncherUtils.navigateToOwnerDashboard(this);
                } else if ("user".equals(userType)) {
                    LauncherUtils.navigateToMainActivity(this);
                } else {
                    LauncherUtils.showToast(this, getString(R.string.unrecognized_user_type_please_log_in_again));
                }
            }
        }

        // Handle edge-to-edge layout insets
        View rootView = findViewById(R.id.firstmain);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}
