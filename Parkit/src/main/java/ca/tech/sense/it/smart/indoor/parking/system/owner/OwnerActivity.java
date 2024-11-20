package ca.tech.sense.it.smart.indoor.parking.system.owner;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.Manager.FragmentManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.Manager.PreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.Manager.SessionDataManager;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.DashboardFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.LocationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.TransactionsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment;

public class OwnerActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private static final String TAG = "OwnerMainActivity";

    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final LocationsFragment locationsFragment = new LocationsFragment();
    private final TransactionsFragment transactionsFragment = new TransactionsFragment();
    private final AccountFragment accountFragment = AccountFragment.newInstance(R.id.fragment_container_owner);
    private FragmentManagerHelper fragmentManagerHelper;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);


        // Fetch session data for owner
        SessionDataManager.getInstance().fetchSessionData((user, owner) -> {
            if (owner != null) {
                Log.d(TAG, "Owner data loaded: " + owner.getEmail());
                // Handle owner-specific logic
            } else {
                Log.d(TAG, "Owner data not found.");
                // Handle no owner data case
            }
        });
        // Initialize UI components
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_owner);
        setSupportActionBar(findViewById(R.id.toolbar_owner));

        // Initialize helpers
        fragmentManagerHelper = new FragmentManagerHelper(getSupportFragmentManager(), R.id.fragment_container_owner);
        preferenceManager = new PreferenceManager(this);

        // Set BottomNavigationView listener
        bottomNavigationView.setOnItemSelectedListener(this);

        // Load the saved fragment or default fragment
        String currentFragment = preferenceManager.getCurrentFragment();
        if (currentFragment.equals("accountFragment")) {
            openFragment(accountFragment, "accountFragment");
        } else {
            openFragment(dashboardFragment, "dashboardFragment");
        }

        // Register OnBackPressedCallback to handle back press behavior
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If there are fragments in the back stack, pop the current fragment
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Otherwise, call default behavior (finish the activity)
                    finish();
                }
            }
        };

        // Add the callback to the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String fragmentTag = "";
        String toolbarTitle = "";

        if (item.getItemId() == R.id.navigation_dashboard) {
            selectedFragment = dashboardFragment;
            fragmentTag = "dashboardFragment";
            toolbarTitle = getString(R.string.dashboard);
        } else if (item.getItemId() == R.id.navigation_transactions) {
            selectedFragment = transactionsFragment;
            fragmentTag = "transactionsFragment";
            toolbarTitle = getString(R.string.transactions);
        } else if (item.getItemId() == R.id.navigation_locations) {
            selectedFragment = locationsFragment;
            fragmentTag = "locationsFragment";
            toolbarTitle = getString(R.string.locations);
        } else if (item.getItemId() == R.id.navigation_account) {
            selectedFragment = accountFragment;
            fragmentTag = "accountFragment";
            toolbarTitle = getString(R.string.my_account);
        }

        if (selectedFragment != null) {
            openFragment(selectedFragment, fragmentTag);
            Objects.requireNonNull(getSupportActionBar()).setTitle(toolbarTitle);  // Update toolbar title
        }

        return true;
    }

    private void openFragment(Fragment fragment, String fragmentTag) {
        fragmentManagerHelper.openFragment(fragment, fragmentTag);
        preferenceManager.saveCurrentFragment(fragmentTag);
    }
}
