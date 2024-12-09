package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleSlot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class SlotListBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView slotsRecyclerView;
    private ProgressBar progressBar;
    private SlotAdapter adapter;
    private List<ParkingSlot> parkingSlots;
    private String locationId; // Location ID passed from the parent fragment
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Button addSlotButton;

    public static SlotListBottomSheetDialogFragment newInstance(String locationId) {
        SlotListBottomSheetDialogFragment fragment = new SlotListBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("locationId", locationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slot_list_bottom_sheet, container, false);

        // Initialize views
        slotsRecyclerView = view.findViewById(R.id.slotsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        addSlotButton = view.findViewById(R.id.addSlotButton);
        mAuth = FirebaseAuthSingleton.getInstance();


        // Get the location ID from the arguments passed by the parent fragment
        if (getArguments() != null) {
            locationId = getArguments().getString("locationId");
        }

        addSlotButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), AddSlotActivity.class);
            intent.putExtra("locationId", locationId);
            startActivity(intent);
            dismiss();
        });

        // Set up RecyclerView
        parkingSlots = new ArrayList<>();
        adapter = new SlotAdapter(parkingSlots);
        slotsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        slotsRecyclerView.setAdapter(adapter);

        // Load data from Firebase
        fetchParkingSlots();

        return view;
    }

    private void fetchParkingSlots() {
        progressBar.setVisibility(View.VISIBLE);
        slotsRecyclerView.setVisibility(View.GONE);

        // Reference to Firebase node containing the slots for the specific location
        databaseReference = FirebaseDatabaseSingleton.getInstance().getReference("owners").child(Objects.requireNonNull(mAuth.getUid()))
                .child("parkingLocationIds").child(locationId).child("slots");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkingSlots.clear();
                for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                    ParkingSlot slot = slotSnapshot.getValue(ParkingSlot.class);
                    if (slot != null) {
                        parkingSlots.add(slot);
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
        if (parkingSlots.isEmpty()) {
            slotsRecyclerView.setVisibility(View.GONE);
            // Optionally show an empty state layout
        } else {
            slotsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }
}
