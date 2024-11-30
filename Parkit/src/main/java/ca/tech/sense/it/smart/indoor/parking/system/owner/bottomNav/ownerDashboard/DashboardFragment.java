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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.LocationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions.TransactionsFragment;

import java.util.Calendar;

public class DashboardFragment extends Fragment {
    private static final String ARG_CONTAINER_VIEW_ID = "containerViewId";
    private int containerViewId;
    private Owner currentOwner;
    private SessionManager sessionManager;
    private Map<Integer, DashboardSection> sectionMap;
    private String userName; // Replace with the actual user name

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(int containerViewId) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CONTAINER_VIEW_ID, containerViewId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            containerViewId = getArguments().getInt(ARG_CONTAINER_VIEW_ID);
        }

        // Initialize the section mapping
        sectionMap = new HashMap<>();
        sectionMap.put(R.id.cardLocations, DashboardSection.LOCATIONS);
        sectionMap.put(R.id.cardTransactions, DashboardSection.TRANSACTIONS);
        sectionMap.put(R.id.cardActiveParkingLot, DashboardSection.ACTIVE_PARKING);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Add the greeting with the user's name
        TextView greetingTextView = view.findViewById(R.id.dashboardGreetingTextView);
        greetingTextView.setText(getGreetingMessageWithUserName());

        setupClickListeners(view);
        return view;
    }

    private void setupClickListeners(View view) {
        for (Map.Entry<Integer, DashboardSection> entry : sectionMap.entrySet()) {
            int layoutId = entry.getKey();
            DashboardSection section = entry.getValue();
            setSectionClickListener(view, layoutId, section);
        }
    }

    private void setSectionClickListener(View view, int layoutId, DashboardSection section) {
        CardView card = view.findViewById(layoutId);
        card.setOnClickListener(v -> handleSectionSelection(section));
    }

    private void handleSectionSelection(DashboardSection section) {
        switch (section) {
            case LOCATIONS:
                openFragment(new LocationsFragment());
                break;
            case TRANSACTIONS:
                openFragment(new TransactionsFragment());
                break;
            case ACTIVE_PARKING:
//                openFragment(new ActiveParkingFragment()); // Uncomment this line once ActiveParkingFragment is created
                break;
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment, getTag());
        fragmentTransaction.addToBackStack(getTag());
        fragmentTransaction.commit();
    }

    // Function to generate greeting based on the time of the day
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

    // Function to generate greeting with the user's name and different colors
    private SpannableString getGreetingMessageWithUserName() {
        sessionManager=SessionManager.getInstance(requireContext());
        currentOwner = sessionManager.getCurrentOwner();
        userName = currentOwner.getFirstName();
        Log.d("DashboardFragment", "User Name: " + userName);
        String greetingMessage = getGreetingMessage() + ", " + userName;
        SpannableString spannableString = new SpannableString(greetingMessage);

        // Change color of the user's name (after the comma)
        int startIndex = greetingMessage.indexOf(",") + 2; // Skip the comma and space
        int endIndex = greetingMessage.length();
        spannableString.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.colorAccent, null)),
                startIndex,
                endIndex,
                0
        );

        return spannableString;
    }

    private enum DashboardSection {
        LOCATIONS,
        TRANSACTIONS,
        ACTIVE_PARKING
    }
}
