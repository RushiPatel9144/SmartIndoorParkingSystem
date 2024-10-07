/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */

package ca.tech.sense.it.smart.indoor.parking.system;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.navigation.NavigationView;

import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Activity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Home;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.Park;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.AccountFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.NotificationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.PrivatePolicyFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.RateUsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.SettingsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer.TermsOfUseFragment;

public class MainActivity extends MenuHandler implements BottomNavigationView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    // Declare a BottomNavigationView
    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    // Create instances of each fragment
    Home firstFragment = new Home();
    Park secondFragment = new Park();
    Activity thirdFragment = new Activity();

    // Create instances of each fragment for navigation drawer
    AccountFragment fourthFragment = new AccountFragment();
    NotificationsFragment notificationsFragment = new NotificationsFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    HelpFragment helpFragment = new HelpFragment();
    PrivatePolicyFragment privatePolicyFragment = new PrivatePolicyFragment();
    TermsOfUseFragment termsOfUseFragment = new TermsOfUseFragment();
    RateUsFragment rateUsFragment = new RateUsFragment();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.nisToolbar); //Ignore red line errors
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.main);
        NavigationView navigationView = findViewById(R.id.nis_nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        // Add DrawerListener to control toolbar visibility
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // Do nothing
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                toolbar.setVisibility(View.VISIBLE); // Hide the toolbar
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                toolbar.setVisibility(View.VISIBLE); // Show the toolbar
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Do nothing
            }
        });


        // Find the BottomNavigationView in the layout
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set this activity as the listener for item selection events in the BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);  // Set the initially selected item

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handling back button press using OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back button logic here, such as showing a dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.are_you_sure_you_want_to_exit)
                        .setCancelable(false)
                        .setTitle(R.string.leaving)
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();  // Close the activity
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();  // Stay in the app
                            }
                        })
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
        // Override the onNavigationItemSelected method
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Get the ID of the selected item
        int itemId = item.getItemId();

        // Replace the current fragment based on the selected item
        if (itemId == R.id.navigation_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, firstFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.navigation_park) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, secondFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.navigation_activity) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, thirdFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.navigation_account) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_manage_account) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,fourthFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }else if (itemId == R.id.nav_notifications) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, notificationsFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, settingsFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_help) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, helpFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_private_policy) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, privatePolicyFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_terms_of_use) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, termsOfUseFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.rate_us) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, rateUsFragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_logout) {
            // Handle logout
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;  // Return false if no match is found
    }
}
