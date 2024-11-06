package ca.tech.sense.it.smart.indoor.parking.system.ui.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.FavoritesAdapter;
import ca.tech.sense.it.smart.indoor.parking.system.utility.FavoriteManager;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private List<LatLng> favoriteLocations;
    private FavoriteManager favoriteManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.rvFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteManager = new FavoriteManager(getContext());
        favoriteLocations = favoriteManager.getFavorites();
        adapter = new FavoritesAdapter(favoriteLocations);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
