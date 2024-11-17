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
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Favorites;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.FavoritesAdapter;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Favorites> favoriteLocations; // Use List<String> to store addresses
    private DatabaseReference databaseRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.rvFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase references
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
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

                    String id = data.child("locationId").getValue(String.class);
                    String address = data.child("address").getValue(String.class);
                    String postalCode = data.child("postalCode").getValue(String.class); // Fetch postal code
                    String name = data.child("name").getValue(String.class);

                    // Add data to the list if the address and name are available
                    if (id != null && address != null && postalCode != null && name != null) {
                        favoriteLocations.add(new Favorites(id, address, name, postalCode));
                    }
                }
                // Update RecyclerView with the fetched data
                updateRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.failed_to_load_favorite_locations) + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView() {
        FavoritesAdapter adapter;
        if (favoriteLocations != null && !favoriteLocations.isEmpty()) {
            // Set the adapter and bind the favorite locations list
            adapter = new FavoritesAdapter(favoriteLocations, databaseRef);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(getContext(),getString(R.string.no_favorite_locations_found), Toast.LENGTH_SHORT).show();
        }
    }
}
