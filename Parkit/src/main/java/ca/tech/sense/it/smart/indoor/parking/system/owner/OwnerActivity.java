package ca.tech.sense.it.smart.indoor.parking.system.owner;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.favoriteManager.FragmentManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.preferenceManager.PreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;

import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions.TransactionsFragment;

import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.LocationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.DashboardFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions.TransactionsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;


public class OwnerActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {


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

        // Initialize UI components
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_owner);
        setSupportActionBar(findViewById(R.id.toolbar_owner));
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.logo);
        Objects.requireNonNull(getSupportActionBar()).setElevation(10);

        handleBackButtonPress();
        // Fetch session data when the activity is created
        SessionManager sessionManager = SessionManager.getInstance(this);
        sessionManager.fetchSessionData((user, owner) -> {

            // You can now use 'user' or 'owner' data for your UI
            if (user != null) {
                Owner currentOwner = sessionManager.getCurrentOwner();
            } else if (owner != null) {

            }

        });

        // Initialize helpers
        fragmentManagerHelper = new FragmentManagerHelper(getSupportFragmentManager(), R.id.fragment_container_owner);
        preferenceManager = new PreferenceManager(this);

        // Set BottomNavigationView listener
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String toolbarTitle = "";

        if (item.getItemId() == R.id.navigation_dashboard) {
            loadFragments(dashboardFragment, "dashboardFragment");
            toolbarTitle = getString(R.string.park_it);
        } else if (item.getItemId() == R.id.navigation_transactions) {
            loadFragments(transactionsFragment,  "transactionsFragment");
            toolbarTitle = getString(R.string.transactions);
        } else if (item.getItemId() == R.id.navigation_locations) {
            loadFragments(locationsFragment,  "locationsFragment");
            toolbarTitle = getString(R.string.locations);
        } else if (item.getItemId() == R.id.navigation_account) {
            loadFragments(accountFragment,  "accountFragment");
            toolbarTitle = getString(R.string.my_account);
        }else {
            return false;
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle("  "+toolbarTitle);  // Update toolbar title
        return true;
    }

    private void handleBackButtonPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container_owner);

                // Check if the current fragment is one of the four specific fragments
                if (currentFragment instanceof DashboardFragment || currentFragment instanceof LocationsFragment ||
                        currentFragment instanceof TransactionsFragment || currentFragment instanceof AccountFragment) {

                    // Show confirmation dialog when one of the specific fragments is visible
                    DialogUtil.showLeaveAppDialog(OwnerActivity.this, getString(R.string.confirm_exit),
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

    private void loadFragments(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_owner,fragment,tag);
        transaction.commit();
    }

}
