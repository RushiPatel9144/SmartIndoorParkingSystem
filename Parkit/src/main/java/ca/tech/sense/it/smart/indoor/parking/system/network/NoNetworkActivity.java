package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class NoNetworkActivity extends AppCompatActivity {

    private static final String TAG = "NoNetworkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_no_network);

        Button retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                finish();
            } else {
                Toast.makeText(this, getString(R.string.network_still_unavailable), Toast.LENGTH_SHORT).show();
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to prevent back press
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


        Button openNetworkSettingsButton = findViewById(R.id.openNetworkSettingsButton);
        openNetworkSettingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNetworkAvailability();
    }

    private void checkNetworkAvailability() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (NetworkUtils.isNetworkAvailable(NoNetworkActivity.this)) {
                    runOnUiThread(this::finish); // Network is back, close the activity
                    scheduler.shutdown();  // Shutdown the scheduler when the network is back
                }
            } catch (Exception e) {
                // Log the exception instead of printing the stack trace
                Log.e(TAG, String.valueOf(R.string.error_checking_network_availability), e);
                scheduler.shutdown();  // Shutdown the scheduler in case of an error
            }
        }, 5, 1, TimeUnit.SECONDS);  // Start immediately, check every 1 second
    }
}
