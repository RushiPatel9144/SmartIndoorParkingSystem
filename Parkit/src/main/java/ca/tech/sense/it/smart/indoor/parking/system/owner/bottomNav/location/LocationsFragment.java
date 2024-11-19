package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

public class LocationsFragment extends Fragment implements LocationAdapter.OnAddLocationClickListener, LocationAdapter.OnItemClickListener {

    private RecyclerView locationsRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private LocationAdapter adapter;
    private List<ParkingLocation> parkingLocations;
    private Button button;

    public LocationsFragment(){
        //
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        // Initialize views
        locationsRecyclerView = view.findViewById(R.id.locationsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        button = view.findViewById(R.id.Button);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        // Simulate data loading
        progressBar.setVisibility(View.VISIBLE);
        locationsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        button.setOnClickListener(v -> addLocation());

        parkingLocations = new ArrayList<>(); // Replace with actual data source

        // Set up RecyclerView
        adapter = new LocationAdapter(parkingLocations, this, this);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        locationsRecyclerView.setAdapter(adapter);

        // Simulate loading delay
        view.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            updateUI();
        }, 2000);

        return view;
    }

    private void updateUI() {
        if (parkingLocations.isEmpty()) {
            locationsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE); // Show Empty State
        } else {
            locationsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    public void addLocation(){
        //
    }
    @Override
    public void onAddLocationClick() {
        addLocation();
    }

    @Override
    public void onItemClick(ParkingLocation location) {
        // Handle item click (navigate to location details or edit)
    }
}
