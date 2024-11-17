package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseActivity;

public class OwnerLoginActivity extends BaseActivity {

    private TextView titleTV;
    private MaterialButton materialButton;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        InitializeUiElements();

        materialButton.setVisibility(View.GONE);
        //Dismiss google sign in button
        titleTV.setText("Owner"); //Changing Title from user to Owner
    }


    //Initializing elements.
    private void InitializeUiElements() {
        titleTV = findViewById(R.id.titleTV);
        materialButton = findViewById(R.id.btnGoogleSignIn);
    }


}