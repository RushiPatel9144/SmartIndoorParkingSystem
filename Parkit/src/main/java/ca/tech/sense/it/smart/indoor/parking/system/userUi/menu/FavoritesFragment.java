package ca.tech.sense.it.smart.indoor.parking.system.userUi.menu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.Favorites;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.adapters.FavoritesAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.Park;

public class FavoritesFragment extends BaseNetworkFragment {

    private List<Favorites> favoriteLocations;
    private Set<String> favoriteLocationIds; // To check for duplicates more efficiently
    private DatabaseReference databaseRef;
    private FavoritesAdapter adapter;
    private LinearLayout noFavoritesLayout;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.rvFavorites);
        noFavoritesLayout = view.findViewById(R.id.noFavoritesLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase references
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle the case where the user is not authenticated
            return view;
        }
        String userId = currentUser.getUid();
        databaseRef = FirebaseDatabaseSingleton.getInstance().getReference("users").child(userId).child("saved_locations");

        // Initialize the adapter
        favoriteLocations = new ArrayList<>();
        favoriteLocationIds = new HashSet<>();
        adapter = new FavoritesAdapter(requireContext(), favoriteLocations, databaseRef, this::navigateToParkFragment);
        recyclerView.setAdapter(adapter);

        // Load favorite locations from Firebase
        loadFavoriteLocations();

        // Update the visibility of the no favorites layout
        updateNoFavoritesLayout();

        return view;
    }

    private void loadFavoriteLocations() {
        databaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                addFavoriteLocation(snapshot);
                updateNoFavoritesLayout();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateFavoriteLocation(snapshot);
                updateNoFavoritesLayout();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                updateNoFavoritesLayout();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.failed_to_load_favorite_locations) + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addFavoriteLocation(DataSnapshot snapshot) {
        String id = snapshot.child("locationId").getValue(String.class);
        String address = snapshot.child("address").getValue(String.class);
        String postalCode = snapshot.child("postalCode").getValue(String.class);
        String name = snapshot.child("name").getValue(String.class);

        if (id != null && address != null && postalCode != null && name != null && !favoriteLocationIds.contains(id)) {
            favoriteLocations.add(new Favorites(id, address, name, postalCode));
            favoriteLocationIds.add(id); // Mark this location as added
            adapter.notifyItemInserted(favoriteLocations.size() - 1);
            updateNoFavoritesLayout();
        }
    }

    private void updateFavoriteLocation(DataSnapshot snapshot) {
        String id = snapshot.child("locationId").getValue(String.class);
        String address = snapshot.child("address").getValue(String.class);
        String postalCode = snapshot.child("postalCode").getValue(String.class);
        String name = snapshot.child("name").getValue(String.class);

        if (id != null && address != null && postalCode != null && name != null) {
            for (int i = 0; i < favoriteLocations.size(); i++) {
                if (favoriteLocations.get(i).getId().equals(id)) {
                    favoriteLocations.set(i, new Favorites(id, address, name, postalCode));
                    adapter.notifyItemChanged(i);
                    updateNoFavoritesLayout();
                    break;
                }
            }
        }
    }


    private void updateNoFavoritesLayout() {
        if (favoriteLocations.isEmpty()) {
            noFavoritesLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noFavoritesLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToParkFragment(Favorites favorite) {
        Park parkFragment = new Park();
        Bundle args = new Bundle();
        args.putString("locationId", favorite.getId());
        parkFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, parkFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // Show BookingBottomSheetDialog after a short delay to ensure the fragment is fully attached
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getParentFragmentManager().findFragmentById(R.id.flFragment) instanceof Park) {
                ((Park) Objects.requireNonNull(getParentFragmentManager().findFragmentById(R.id.flFragment)))
                        .showBookingBottomSheet(favorite.getId());
            }
        }, 500);
    }
}
