package ca.tech.sense.it.smart.indoor.parking.system;

import static ca.tech.sense.it.smart.indoor.parking.system.R.string.notification_permission_denied;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.UserLoginActivity;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.UserManager;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Activity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Home;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Park;
import ca.tech.sense.it.smart.indoor.parking.system.ui.menu.MenuHandler;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;

public class MainActivity extends MenuHandler implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private final Home homeFragment = new Home();
    private final Park parkFragment = new Park();
    private final Activity activityFragment = new Activity();
    private final ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment accountFragment = new ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment(R.id.flFragment);
    private static final String PREFS_NAME = "MyAppPreferences";
    private static final String KEY_WELCOME_NOTIFICATION_TIMESTAMP = "welcome_notification_timestamp";
    private static final long NOTIFICATION_COOLDOWN = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    private static final String PREFS__NAME = "UserPrefs";
    private static final String KEY_WELCOME_NOTIFICATION_SENT = "welcome_notification_sent";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyTheme();

        // Initialize Firebase Authentication
        initFirebaseAuth();

        // Initialize UI components
        initUIComponents();

        // Initialize and fetch user data once in MainActivity
        UserManager.getInstance().fetchUserData(user -> {
            if (user != null) {
                Log.d("MainActivity", "User data loaded: " + user.getEmail());
                // You can update the UI or continue with app flow
            } else {
                Log.d("MainActivity", "Failed to load user data.");
            }
        });

        // Set BottomNavigationView listener
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Request notification permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        // Handle back button press
        handleBackButtonPress();

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Send welcome notifications
        sendWelcomeBackNotification();
        sendNewUserWelcomeNotification();
    }

    private void initFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            navigateToLoginActivity();
        }
    }

    private void initUIComponents() {
        toolbar = findViewById(R.id.nisToolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void handleBackButtonPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.are_you_sure_you_want_to_exit)
                            .setCancelable(false)
                            .setTitle(R.string.leaving)
                            .setIcon(R.drawable.alert)
                            .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_home) {
            loadFragments(homeFragment);
            toolbar.setTitle(R.string.home);
        } else if (itemId == R.id.navigation_park) {
            loadFragments(parkFragment);
            toolbar.setTitle(R.string.park);
        } else if (itemId == R.id.navigation_activity) {
            loadFragments(activityFragment);
            toolbar.setTitle(R.string.activity);
        } else if (itemId == R.id.navigation_account) {
            loadFragments(accountFragment);
            toolbar.setTitle(R.string.my_account);
        } else {
            return false;
        }
        return true;
    }

    private void loadFragments(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), UserLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("InlinedApi")
    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
        } else {
            // Permission already granted, proceed with notification
            sendWelcomeBackNotification(); // Or any notification logic
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendNewUserWelcomeNotification();
            } else {
                Toast.makeText(this, notification_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendWelcomeBackNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastSentTimestamp = sharedPreferences.getLong(KEY_WELCOME_NOTIFICATION_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && (currentTime - lastSentTimestamp > NOTIFICATION_COOLDOWN)) {
            NotificationHelper.sendNotification(
                    this,
                    getString(R.string.welcome_back),
                    getString(R.string.we_ve_missed_you_check_out_the_latest_parking_spots_available_for_you),
                    currentUser.getUid()
            );
            sharedPreferences.edit().putLong(KEY_WELCOME_NOTIFICATION_TIMESTAMP, currentTime).apply();
        } else if (currentUser == null) {
            Log.d("MainActivity", "No user is currently signed in.");
        }
    }


    private void sendNewUserWelcomeNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS__NAME, MODE_PRIVATE);
        boolean isWelcomeNotificationSent = sharedPreferences.getBoolean(KEY_WELCOME_NOTIFICATION_SENT, false);

        if (!isWelcomeNotificationSent) {
            NotificationHelper.sendNotification(
                    this,
                    getString(R.string.welcome_to_parkit),
                    getString(R.string.explore_the_app_and_find_parking_spots_nearby),
                    Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()
            );

            // Set flag to true to avoid sending again
            sharedPreferences.edit().putBoolean(KEY_WELCOME_NOTIFICATION_SENT, true).apply();
        }
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.dark_theme), false);
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
