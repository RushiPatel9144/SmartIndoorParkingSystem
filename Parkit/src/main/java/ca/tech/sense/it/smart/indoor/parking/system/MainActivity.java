package ca.tech.sense.it.smart.indoor.parking.system;

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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.tech.sense.it.smart.indoor.parking.system.Manager.NotificationManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.Manager.ThemeManager;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.LoginActivity;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.UserManager;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Activity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Home;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Park;
import ca.tech.sense.it.smart.indoor.parking.system.ui.menu.MenuHandler;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;

public class MainActivity extends MenuHandler implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private final Home homeFragment = new Home();
    private final Park parkFragment = new Park();
    private final Activity activityFragment = new Activity();
    private final ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment accountFragment = ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment.newInstance(R.id.flFragment);
    private static final String PREFS_NAME = "MyAppPreferences";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private ThemeManager themeManager;
    private NotificationManagerHelper notificationManagerHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        themeManager = new ThemeManager(this);
        notificationManagerHelper = new NotificationManagerHelper(this);

        themeManager.applyTheme();

        initFirebaseAuth();
        initUIComponents();

        UserManager.getInstance().fetchUserData(user -> {
            if (user != null) {
                Log.d("MainActivity", "User data loaded: " + user.getEmail());
            } else {
                Log.d("MainActivity", "Failed to load user data.");
            }
        });

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        handleBackButtonPress();
        NotificationHelper.createNotificationChannel(this);

        notificationManagerHelper.sendWelcomeBackNotification();
        notificationManagerHelper.sendNewUserWelcomeNotification();

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
                    DialogUtil.showLeaveAppDialog(MainActivity.this, getString(R.string.confirm_exit), getString(R.string.are_you_sure_you_want_to_exit_the_app), R.drawable.crisis,
                            new DialogUtil.BackPressCallback() {
                                @Override
                                public void onConfirm() {
                                    finishAffinity();
                                }

                                @Override
                                public void onCancel() {
                                    //dismiss
                                }
                            });}
            }
        };getOnBackPressedDispatcher().addCallback(this, callback);
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
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("InlinedApi")
    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
        } else {
            notificationManagerHelper.sendWelcomeBackNotification();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationManagerHelper.sendNewUserWelcomeNotification();
            } else {
                Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

}

