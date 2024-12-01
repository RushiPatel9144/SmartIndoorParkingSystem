package ca.tech.sense.it.smart.indoor.parking.system;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

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

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.FirstActivity;
import ca.tech.sense.it.smart.indoor.parking.system.manager.notificationManager.NotificationManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.themeManager.ThemeManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
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

        // Fetch session data when the activity is created
        SessionManager sessionManager = SessionManager.getInstance(this);
        sessionManager.fetchSessionData((user, owner) -> {
            // You can now use 'user' or 'owner' data for your UI
            if (user != null) {
                // Use user data
            } else if (owner != null) {
                // Use owner data
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
        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.logo);
        toolbar.setTitle("  " + getString(R.string.park_it));
        toolbar.setElevation(10); // Adds elevation for a shadow effect
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void handleBackButtonPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.flFragment);

                // Check if the current fragment is one of the four specific fragments
                if (currentFragment instanceof Home || currentFragment instanceof Park ||
                        currentFragment instanceof Activity || currentFragment instanceof AccountFragment) {

                    // Show confirmation dialog when one of the specific fragments is visible
                    DialogUtil.showLeaveAppDialog(MainActivity.this, getString(R.string.confirm_exit),
                            getString(R.string.are_you_sure_you_want_to_exit_the_app), R.drawable.crisis,
                            new DialogUtil.BackPressCallback() {
                                @Override
                                public void onConfirm() {
                                    finishAffinity(); // Exit the app
                                }

                                @Override
                                public void onCancel() {
                                    // Dismiss the dialog
                                }
                            });
                } else {
                    // Pop the fragment from the back stack if it's not one of the specific fragments
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack();
                    } else {
                        // Default behavior if no fragments are left in the back stack
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        String toolbarTitle = "";
        if (itemId == R.id.navigation_home) {
            loadFragments(homeFragment, "HomeFragment");
            toolbarTitle = getString(R.string.home);
        } else if (itemId == R.id.navigation_park) {
            loadFragments(parkFragment, "ParkFragment");
            toolbarTitle = getString(R.string.park);
        } else if (itemId == R.id.navigation_activity) {
            loadFragments(activityFragment, "ActivityFragment");
            toolbarTitle = getString(R.string.activity);
        } else if (itemId == R.id.navigation_account) {
            loadFragments(accountFragment, "AccountFragment");
            toolbarTitle = getString(R.string.my_account);
        } else {
            return false;
        }
        toolbar.setTitle("  "+ toolbarTitle);
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

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
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

