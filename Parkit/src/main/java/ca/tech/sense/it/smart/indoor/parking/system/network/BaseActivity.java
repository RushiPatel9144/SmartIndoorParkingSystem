package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        NetworkManager.getInstance().startMonitoring(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NetworkManager.getInstance().stopMonitoring(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Intent intent = new Intent(this, NoNetworkActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Ensures the activity starts from outside of an activity context if needed
            startActivity(intent);
            finish(); // finish the current activity to prevent the user from navigating back to it without internet
        } else {
            NetworkManager.getInstance().startMonitoring(this);
        }
    }

}