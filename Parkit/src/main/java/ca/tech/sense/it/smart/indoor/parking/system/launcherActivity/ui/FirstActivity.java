package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first);

        // Handle window insets for proper UI layout on edge-to-edge screens
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.firstmain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the buttons and set click listeners
        findViewById(R.id.firstSignInBut).setOnClickListener(this::goToLoginAsUser);
        findViewById(R.id.firstSignUpAsOwnerBut).setOnClickListener(this::goToLoginAsOwner);
    }

    // Navigate to Login Activity as User
    public void goToLoginAsUser(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("login_as", "user"); // Pass the type of login
        startActivity(intent);
    }

    // Navigate to Login Activity as Owner
    public void goToLoginAsOwner(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("login_as", "owner"); // Pass the type of login
        startActivity(intent);
    }
}
