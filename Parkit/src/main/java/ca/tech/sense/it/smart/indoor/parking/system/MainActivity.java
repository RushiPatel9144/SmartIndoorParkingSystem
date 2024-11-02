/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */

package ca.tech.sense.it.smart.indoor.parking.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LoginActivity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Activity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Home;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Park;
import ca.tech.sense.it.smart.indoor.parking.system.ui.menu.MenuHandler;
import ca.tech.sense.it.smart.indoor.parking.system.utility.NotificationHelper;

public class MainActivity extends MenuHandler implements NavigationBarView.OnItemSelectedListener {

    // Declare a BottomNavigationView
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    // Fragments for bottom navigation
    private final Home homeFragment = new Home();
    private final Park parkFragment = new Park();
    private final Activity activityFragment = new Activity();
    private final ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment accountFragment = new ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment();

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

        // Set BottomNavigationView listener
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);



        // Handle back button press
        handleBackButtonPress();
    }

    private void initFirebaseAuth() {
        FirebaseAuth tempAuth;
        FirebaseUser tempUser;
        tempAuth = FirebaseAuth.getInstance();
        tempUser = tempAuth.getCurrentUser();

        if (tempUser == null) {
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
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.are_you_sure_you_want_to_exit)
                        .setCancelable(false)
                        .setTitle(R.string.leaving)
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
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
            return true;
        } else if (itemId == R.id.navigation_park) {
            loadFragments(parkFragment);
            toolbar.setTitle(R.string.park);
            return true;
        } else if (itemId == R.id.navigation_activity) {
            loadFragments(activityFragment);
            toolbar.setTitle(R.string.activity);
            return true;
        } else if (itemId == R.id.navigation_account) {
            loadFragments(accountFragment);
            toolbar.setTitle(R.string.my_account);
            return true;
        } else {
            return false;
        }
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
