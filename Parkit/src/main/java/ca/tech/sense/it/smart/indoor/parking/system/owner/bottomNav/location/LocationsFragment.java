package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import static ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.AddLocationValidator.validatePrice;
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
import ca.tech.sense.it.smart.indoor.parking.system.manager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.AddLocationActivity;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.LocationAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation.SwipeToDeleteCallback;
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleSlot.SlotListBottomSheetDialogFragment;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class LocationsFragment extends Fragment {

    private RecyclerView locationsRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private static LocationAdapter adapter;
    private List<ParkingLocation> parkingLocations;
    private FirebaseAuth oAuth;
    private FloatingActionButton addButton;
    private final ParkingLocationManager parkingLocationManager = new ParkingLocationManager();

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
        adapter = new LocationAdapter(parkingLocations, new LocationAdapter.OnItemClickListener() {
            @Override
            public void onChangePriceClick(String locationId, int position) {
                changePrice(locationId);
            }

            @Override
            public void onAddSlotsClick(String locationId, int position) {
                SlotListBottomSheetDialogFragment bottomSheetFragment =
                        SlotListBottomSheetDialogFragment.newInstance(locationId);
                bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
            }
        });

        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        locationsRecyclerView.setAdapter(adapter);

        // Add swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter, oAuth.getUid(), parkingLocationManager));
        itemTouchHelper.attachToRecyclerView(locationsRecyclerView);

        // Load data from Firebase
        loadParkingLocations();

        return view;
    }

    private void loadParkingLocations() {
        progressBar.setVisibility(View.VISIBLE);
        locationsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        parkingLocationManager.fetchParkingLocationsByOwnerId(oAuth.getUid(), new ParkingInterface.ParkingLocationFetchCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFetchSuccess(List<ParkingLocation> locations) {
                parkingLocations.clear();
                parkingLocations.addAll(locations);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                updateUI();
            }

            @Override
            public void onFetchFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                showError(errorMessage);
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
    public void onResume() {
        super.onResume();
        loadParkingLocations();
    }

    public void changePrice(String locationId) {
        DialogUtil.showInputDialog(
                requireContext(),
                getString(R.string.update_price) ,
                getString(R.string.input_price_in_cad),
                new DialogUtil.InputDialogCallback() {
                    @Override
                    public void onConfirm(String input) {
                        double price = validatePrice(requireContext(), input);
                        if (price >= 0) {
                            updatePrice(locationId, price);
                        }
                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(requireContext(), R.string.price_update_canceled , Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
        private void updatePrice(String locationId, double price) {
            parkingLocationManager.changePrice(requireContext(), Objects.requireNonNull(oAuth.getUid()), locationId, price);
            Toast.makeText(requireContext(), R.string.price_updated_successfully, Toast.LENGTH_SHORT).show();
        }
}
