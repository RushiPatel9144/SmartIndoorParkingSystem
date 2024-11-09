package ca.tech.sense.it.smart.indoor.parking.system.owner;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.DashboardFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.LocationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.TransactionsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountFragment;

public class OwnerActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private Toolbar toolbar;
    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final LocationsFragment locationsFragment = new LocationsFragment();
    private final TransactionsFragment transactionsFragment = new TransactionsFragment();
    private final AccountFragment accountFragment = new AccountFragment(R.id.fragment_container_owner);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        // Initialize UI components
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_owner);
        toolbar = findViewById(R.id.toolbar_owner);

        // Set the toolbar as the action bar
        setSupportActionBar(toolbar);

        // Set BottomNavigationView listener
        bottomNavigationView.setOnItemSelectedListener(this);

        // Load default fragment (Dashboard)
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String toolbarTitle = "";

        if (item.getItemId() == R.id.navigation_dashboard) {
            selectedFragment = dashboardFragment;
            toolbarTitle = getString(R.string.dashboard);
        } else if (item.getItemId() == R.id.navigation_transactions) {
            selectedFragment = transactionsFragment;
            toolbarTitle = getString(R.string.transactions);
        } else if (item.getItemId() == R.id.navigation_locations) {
            selectedFragment = locationsFragment;
            toolbarTitle = getString(R.string.locations);
        } else if (item.getItemId() == R.id.navigation_account) {
            selectedFragment = accountFragment;
            toolbarTitle = getString(R.string.my_account);
        }

        // Replace the fragment
        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_owner, selectedFragment)
                    .commit();
        }

        // Update the toolbar title
        toolbar.setTitle(toolbarTitle);

        return true;
    }
}
