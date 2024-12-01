package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.parkingHistory.ParkingHistoryAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.parkingHistory.ParkingHistoryModel;

public class DashboardFragment extends Fragment {
    private static final String ARG_CONTAINER_VIEW_ID = "containerViewId";
    private int containerViewId;
    private SessionManager sessionManager;
    private String userName; // Owner name will be stored here

    private RecyclerView recyclerView;
    private ParkingHistoryAdapter parkingHistoryAdapter;
    private List<ParkingHistoryModel> parkingHistoryList;
    private FirebaseFirestore db;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            containerViewId = getArguments().getInt(ARG_CONTAINER_VIEW_ID);
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        parkingHistoryList = new ArrayList<>();
        parkingHistoryAdapter = new ParkingHistoryAdapter(getContext(), parkingHistoryList);

        // Fetch session data when the activity is created
        sessionManager = SessionManager.getInstance(requireContext());
        sessionManager.fetchSessionData((user, owner) -> {
            if (user != null) {
                // Use user data if needed
            } else if (owner != null) {
                Owner currentOwner = sessionManager.getCurrentOwner();
                Log.d("DashboardFragment", "Owner Name: " + currentOwner.getFirstName());
                userName = currentOwner.getFirstName(); // Store owner's first name

                // Add the greeting with the owner's name
                TextView greetingTextView = getView().findViewById(R.id.dashboardGreetingTextView);
                greetingTextView.setText(getGreetingMessageWithUserName());
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.activeparkingRecyclerView); // Ensure the ID matches your RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(parkingHistoryAdapter);

        // SwipeRefreshLayout for refreshing the content
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Logic for refreshing data goes here
            Toast.makeText(getContext(), "Refreshing dashboard...", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false); // Stop refreshing
        });

        // Fetch parking history data from Firestore
        fetchParkingHistoryData();

        return view;
    }

    private void fetchParkingHistoryData() {
        db.collection("ParkingHistory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract data from the document
                            String locationName = document.getString("locationName");
                            String slotName = document.getString("slotName");
                            String paymentAmount = document.getString("paymentAmount");
                            String usageTime = document.getString("usageTime");
                            String userProfilePicUrl = document.getString("userProfilePicUrl");

                            // Create ParkingHistory object
                            ParkingHistoryModel history = new ParkingHistoryModel(locationName, slotName, paymentAmount, usageTime, userProfilePicUrl);
                            parkingHistoryList.add(history);
                        }
                        // Notify the adapter that the data has changed
                        parkingHistoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("DashboardFragment", "Error getting documents: ", task.getException());
                    }
                });
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

    // Function to generate greeting with the owner's name and different colors
    private SpannableString getGreetingMessageWithUserName() {
        String greetingMessage = getGreetingMessage() + ", " + userName;
        SpannableString spannableString = new SpannableString(greetingMessage);

        // Change color of the owner's name (after the comma)
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
}
