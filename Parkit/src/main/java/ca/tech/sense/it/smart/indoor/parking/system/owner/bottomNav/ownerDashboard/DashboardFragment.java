package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.TransactionManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.LocationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.parkingHistory.ParkingHistoryAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.parkingHistory.ParkingHistoryModel;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions.TransactionsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.NotificationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount.ManageAccountFragment;

public class DashboardFragment extends Fragment {

    private SessionManager sessionManager;
    private String userName;
    private TextView dashboardEarnings;
    private MaterialButton editProfileButton;
    private MaterialButton transactionsButton;

    private ParkingHistoryAdapter parkingHistoryAdapter;
    private List<ParkingHistoryModel> parkingHistoryList;
    private FirebaseFirestore db;
    private TransactionManager transactionManager;
    private CardView cardNotfication;
    private CardView cardHelp;
    private CardView cardActiveLots;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionManager = new TransactionManager(FirebaseDatabaseSingleton.getInstance());
        sessionManager = SessionManager.getInstance(requireContext());

        initializeUI();

    }

    private void setUpOnClickListeners(BottomNavigationView bottomNavigationView) {
        sessionManager.fetchSessionData((user, owner) -> {
            if (user != null) {
                // Handle user data if needed
            } else if (owner != null) {
                setOwnerData(owner);
            }
        });

        // Set up button click listeners
        editProfileButton.setOnClickListener(v -> {
            bottomNavigationView.setSelectedItemId(R.id.navigation_account);
            loadFragments(new ManageAccountFragment(), "manage_account_fragment");
        });
        transactionsButton.setOnClickListener(v -> {
            bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);
            loadFragments(new TransactionsFragment(), "transactions_fragment");
        } );

        cardHelp.setOnClickListener( v -> {
            // Load NotificationsFragment directly
            bottomNavigationView.setSelectedItemId(R.id.navigation_account);
            loadFragments(new HelpFragment(), "help_fragment");


        });
        cardNotfication.setOnClickListener( v -> {
            // Load NotificationsFragment directly
            bottomNavigationView.setSelectedItemId(R.id.navigation_account);
            loadFragments(new NotificationsFragment(), "notifications_fragment");

        });

        cardActiveLots.setOnClickListener( v -> {
            bottomNavigationView.setSelectedItemId(R.id.navigation_locations);
            loadFragments(new LocationsFragment(), "locations_fragment");
        });
    }


    private void setOwnerData(Owner currentOwner) {
        userName = currentOwner.getFirstName(); // Store owner's first name
        displayOwnerGreeting();
        fetchOwnerIncome(currentOwner.getUid());
    }

    private void displayOwnerGreeting() {
        TextView greetingTextView = requireView().findViewById(R.id.dashboardGreetingTextView);
        greetingTextView.setText(getGreetingMessageWithUserName());
    }

    private void fetchOwnerIncome(String ownerUid) {
        dashboardEarnings = requireView().findViewById(R.id.dashboard_earnings_value);
        transactionManager.retrieveOwnerTotalIncome(ownerUid, new TransactionManager.FetchIncomeCallback() {
            @Override
            public void onSuccess(String totalIncome) {
                dashboardEarnings.setText(totalIncome);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUI() {
        db = FirebaseFirestore.getInstance();
        parkingHistoryList = new ArrayList<>();
        parkingHistoryAdapter = new ParkingHistoryAdapter(getContext(), parkingHistoryList);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        setUpRecyclerView(view);
        setUpSwipeRefresh(view);

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation_owner);
        editProfileButton = view.findViewById(R.id.dashboard_edit_profile_button);
        transactionsButton = view.findViewById(R.id.dashboard_transactions_button);
        cardNotfication = view.findViewById(R.id.cardNotification);
        cardHelp = view.findViewById(R.id.cardHelp);
        cardActiveLots = view.findViewById(R.id.cardActiveLots);



        setUpOnClickListeners(bottomNavigationView);


        return view;
    }

    private void setUpRecyclerView(View view) {
        RecyclerView recyclerView;
        recyclerView = view.findViewById(R.id.activeparkingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(parkingHistoryAdapter);
    }

    private void setUpSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getContext(), "Refreshing dashboard...", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void fetchParkingHistoryData() {
        db.collection("ParkingHistory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ParkingHistoryModel history = new ParkingHistoryModel(
                                    document.getString("locationName"),
                                    document.getString("slotName"),
                                    document.getString("paymentAmount"),
                                    document.getString("usageTime"),
                                    document.getString("userProfilePicUrl")
                            );
                            parkingHistoryList.add(history);
                        }
                        parkingHistoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("DashboardFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private String getGreetingMessage() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 5 && currentHour < 12) {
            return "Good Morning";
        } else if (currentHour >= 12 && currentHour < 18) {
            return "Good Afternoon";
        } else if (currentHour >= 18 && currentHour < 21) {
            return "Good Evening";
        } else {
            return "Catch Some Zzzs";
        }
    }

    private SpannableString getGreetingMessageWithUserName() {
        String greetingMessage = getGreetingMessage() + ", " + userName;
        SpannableString spannableString = new SpannableString(greetingMessage);

        int startIndex = greetingMessage.indexOf(",") + 2;
        int endIndex = greetingMessage.length();
        spannableString.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.colorAccent, null)),
                startIndex,
                endIndex,
                0
        );

        return spannableString;
    }

    // Method to load the fragment
    private void loadFragments(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getParentFragmentManager() ;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_owner, fragment, tag);
        transaction.addToBackStack(null);  // Optional: Add fragment to back stack for back navigation
        transaction.commit();
    }
}
