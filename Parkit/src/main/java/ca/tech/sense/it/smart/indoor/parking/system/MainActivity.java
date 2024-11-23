package ca.tech.sense.it.smart.indoor.parking.system;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.tech.sense.it.smart.indoor.parking.system.manager.NotificationManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.SessionDataManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.ThemeManager;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.FirstActivity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Activity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Home;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Park;
import ca.tech.sense.it.smart.indoor.parking.system.ui.menu.MenuHandler;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;

public class MainActivity extends MenuHandler implements NavigationBarView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private final Home homeFragment = new Home();
    private final Park parkFragment = new Park();
    private final Activity activityFragment = new Activity();
    private final AccountFragment accountFragment = AccountFragment.newInstance(R.id.flFragment);
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

        // Fetch session data (user or owner)
        SessionDataManager.getInstance().fetchSessionData((user, owner) -> {
            if (user != null) {
                Log.d(TAG, "User data loaded: " + user.getEmail());
                // Handle user-specific logic
            } else if (owner != null) {
                Log.d(TAG, "Owner data loaded: " + owner.getEmail());
                // Handle owner-specific logic
            } else {
                Log.d(TAG, "No session data found.");
                // Handle no data case (perhaps navigate to login screen)
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
            navigateToFirstActivity();
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
            loadFragments(homeFragment, "HomeFragment");
            toolbar.setTitle(R.string.home);
        } else if (itemId == R.id.navigation_park) {
            loadFragments(parkFragment, "ParkFragment");
            toolbar.setTitle(R.string.park);
        } else if (itemId == R.id.navigation_activity) {
            loadFragments(activityFragment, "ActivityFragment");
            toolbar.setTitle(R.string.activity);
        } else if (itemId == R.id.navigation_account) {
            loadFragments(accountFragment, "AccountFragment");
            toolbar.setTitle(R.string.my_account);
        } else {
            return false;
        }
        return true;
    }

    private void loadFragments(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flFragment,fragment,tag);
        transaction.commit();
    }



    private void navigateToFirstActivity() {
        Intent intent = new Intent(getApplicationContext(), FirstActivity.class);
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

