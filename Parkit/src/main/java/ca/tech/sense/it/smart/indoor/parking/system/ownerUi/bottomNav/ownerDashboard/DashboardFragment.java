package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.ownerDashboard;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.databinding.FragmentDashboardBinding;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.TransactionManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.LocationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.transactions.TransactionsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.RateUsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.manageAccount.ManageAccountFragment;

public class DashboardFragment extends Fragment {

    private SessionManager sessionManager;
    private String userName;
    private TextView dashboardEarnings;
    private MaterialButton editProfileButton;
    private MaterialButton transactionsButton;
    private FirebaseFirestore db;
    private TransactionManager transactionManager;
    private Button help;
    private Button feedback;
    private Button location;
    private FragmentDashboardBinding binding;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionManager = new TransactionManager(FirebaseDatabaseSingleton.getInstance());
        sessionManager = SessionManager.getInstance(requireContext());
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setUpSwipeRefresh(view);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation_owner);
        editProfileButton = binding.dashboardEditProfileButton;
        transactionsButton = binding.dashboardTransactionsButton;
        help = binding.btnGetHelp;
        feedback = binding.btnRateExperience;
        location = binding.btnManageLocationSlots;
        setUpOnClickListeners(bottomNavigationView);
        return view;
    }

    private void setUpOnClickListeners(BottomNavigationView bottomNavigationView) {
        sessionManager.fetchSessionData((user, owner) -> {
            if (owner != null) {
                setOwnerData(owner);
            }
        });

        editProfileButton.setOnClickListener(v -> navigateToFragment(new ManageAccountFragment(), bottomNavigationView, R.id.navigation_account));
        transactionsButton.setOnClickListener(v -> navigateToFragment(new TransactionsFragment(), bottomNavigationView, R.id.navigation_transactions));
        help.setOnClickListener(v -> navigateToFragment(new HelpFragment(), bottomNavigationView, R.id.navigation_account));
        feedback.setOnClickListener(v -> navigateToFragment(new RateUsFragment(), bottomNavigationView, R.id.navigation_account));
        location.setOnClickListener(v -> navigateToFragment(new LocationsFragment(), bottomNavigationView, R.id.navigation_locations));
    }

    private void navigateToFragment(Fragment fragment, BottomNavigationView bottomNavigationView, int menuItemId) {
        bottomNavigationView.setSelectedItemId(menuItemId);
        loadFragments(fragment);
    }

    private void setOwnerData(Owner currentOwner) {
        userName = currentOwner.getFirstName();
        displayOwnerGreeting();
        fetchOwnerIncome(currentOwner.getUid());
    }

    private void displayOwnerGreeting() {
        String greetingMessage = generateGreetingMessage() + ", " + userName;
        SpannableString spannableString = new SpannableString(greetingMessage);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent, null)), greetingMessage.indexOf(",") + 2, greetingMessage.length(), 0);
        binding.dashboardGreetingTextView.setText(spannableString);
    }

    private void fetchOwnerIncome(String ownerUid) {
        dashboardEarnings = binding.dashboardEarningsValue;
        transactionManager.retrieveOwnerTotalIncome(ownerUid, new TransactionManager.FetchIncomeCallback() {
            @Override
            public void onSuccess(String totalIncome) {
                dashboardEarnings.setText(totalIncome);
            }

            @Override
            public void onFailure(String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    private void showError(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String generateGreetingMessage() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 5 && currentHour < 12) {
            return getString(R.string.good_morning);
        } else if (currentHour >= 12 && currentHour < 18) {
            return getString(R.string.good_afternoon);
        } else if (currentHour >= 18 && currentHour < 21) {
            return getString(R.string.good_evening);
        } else {
            return getString(R.string.good_night);
        }
    }

    private void loadFragments(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_owner, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setUpSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getContext(), getString(R.string.refreshing_dashboard), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });
    }
}
