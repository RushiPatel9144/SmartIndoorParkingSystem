package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.AddLocationActivity;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.LocationAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.SwipeToDeleteCallback;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleSlot.SlotListBottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LocationsFragment extends Fragment implements LocationAdapter.OnItemClickListener {

    private RecyclerView locationsRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private LocationAdapter adapter;
    private List<ParkingLocation> parkingLocations;
    private FirebaseAuth oAuth;
    private FloatingActionButton addButton;
    private ParkingLocationManager parkingLocationManager = new ParkingLocationManager();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Button button;
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        // Initialize views
        locationsRecyclerView = view.findViewById(R.id.locationsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        button = view.findViewById(R.id.addLocationEmptyStateButton);
        addButton = view.findViewById(R.id.addLocationButton);

        button.setOnClickListener(v -> startAddLocationActivity());
        addButton.setOnClickListener(v -> startAddLocationActivity());

        oAuth = FirebaseAuthSingleton.getInstance();

        // Set up RecyclerView
        parkingLocations = new ArrayList<>();
        adapter = new LocationAdapter(parkingLocations, this);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        locationsRecyclerView.setAdapter(adapter);

        // Add swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter,oAuth.getUid(),parkingLocationManager));
        itemTouchHelper.attachToRecyclerView(locationsRecyclerView);

        // Load data from Firebase
        fetchParkingLocations();

        return view;
    }

        private void fetchParkingLocations() {
            DatabaseReference databaseReference;
            progressBar.setVisibility(View.VISIBLE);
            locationsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);

            databaseReference = FirebaseDatabaseSingleton.getInstance().getReference("owners")
                    .child(Objects.requireNonNull(oAuth.getUid())).child("parkingLocationIds");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    parkingLocations.clear();
                    for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                        ParkingLocation location = locationSnapshot.getValue(ParkingLocation.class);
                        if (location != null) {
                            parkingLocations.add(location);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    showError(error.getMessage());
                }
            });
        }

    private void updateUI() {
        if (parkingLocations.isEmpty()) {
            addButton.setVisibility(View.GONE);
            locationsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            locationsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    private void startAddLocationActivity() {
        Intent intent = new Intent(getContext(), AddLocationActivity.class);
        startActivity(intent);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(ParkingLocation location, int position) {
        SlotListBottomSheetDialogFragment bottomSheetFragment =
                SlotListBottomSheetDialogFragment.newInstance(location.getId());
        bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchParkingLocations();
    }
}
