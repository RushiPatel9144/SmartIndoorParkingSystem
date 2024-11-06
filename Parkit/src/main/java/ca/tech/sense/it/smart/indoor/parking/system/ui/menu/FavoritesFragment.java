/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.FavoritesAdapter;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private List<String> favoriteLocations; // Use List<String> to store addresses
    private DatabaseReference databaseRef;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.rvFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase references
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("saved_locations");

        // Load favorite locations from Firebase
        loadFavoriteLocations();

        return view;
    }

    private void loadFavoriteLocations() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteLocations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    // Retrieve data from Firebase
                    String address = data.child("address").getValue(String.class);

                    // Add data to the list if the address is available
                    if (address != null) {
                        favoriteLocations.add(address);
                    }
                }

                // Update RecyclerView with the fetched data
                updateRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load favorite locations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView() {
        if (favoriteLocations != null && !favoriteLocations.isEmpty()) {
            // Set the adapter and bind the favorite locations list
            adapter = new FavoritesAdapter(favoriteLocations);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(getContext(), "No favorite locations found.", Toast.LENGTH_SHORT).show();
        }
    }
}
