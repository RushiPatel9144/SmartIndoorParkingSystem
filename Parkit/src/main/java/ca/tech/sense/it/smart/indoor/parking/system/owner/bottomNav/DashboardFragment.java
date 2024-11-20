package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.LocationsFragment;

import androidx.cardview.widget.CardView;  // Import CardView

public class DashboardFragment extends Fragment {
    private static final String ARG_CONTAINER_VIEW_ID = "containerViewId";
    private int containerViewId;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        setupClickListeners(view);

        return view;
    }

    private void setupClickListeners(View view) {
        setSectionClickListener(view, R.id.cardLocations, DashboardSection.LOCATIONS);
        setSectionClickListener(view, R.id.cardTransactions, DashboardSection.TRANSACTIONS);
        setSectionClickListener(view, R.id.cardActiveParkingLot, DashboardSection.ACTIVE_PARKING);
    }

    private void setSectionClickListener(View view, int layoutId, DashboardSection section) {
        // Change LinearLayout to CardView
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
//                openFragment(new ActiveParkingFragment());
                break;
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment,getTag());
        fragmentTransaction.addToBackStack(getTag());
        fragmentTransaction.commit();
    }

    private enum DashboardSection {
        LOCATIONS,
        TRANSACTIONS,
        ACTIVE_PARKING
    }
}
