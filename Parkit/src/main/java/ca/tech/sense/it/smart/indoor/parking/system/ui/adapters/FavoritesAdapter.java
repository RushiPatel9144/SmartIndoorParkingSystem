package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<LatLng> favoriteLocationList;

    public FavoritesAdapter(List<LatLng> favoriteLocationList) {
        this.favoriteLocationList = favoriteLocationList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        LatLng favoriteLocation = favoriteLocationList.get(position);
        holder.tvFavoriteTitle.setText("Favorite Location " + (position + 1));
        holder.tvFavoriteAddress.setText("Lat: " + favoriteLocation.latitude + ", Lng: " + favoriteLocation.longitude);
        holder.tvFavoritePostalCode.setText("Postal Code: N/A"); // Placeholder, update with actual postal code if available
    }

    @Override
    public int getItemCount() {
        return favoriteLocationList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvFavoriteTitle;
        TextView tvFavoriteAddress;
        TextView tvFavoritePostalCode;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFavoriteTitle = itemView.findViewById(R.id.tvFavoriteTitle);
            tvFavoriteAddress = itemView.findViewById(R.id.tvFavoriteAddress);
            tvFavoritePostalCode = itemView.findViewById(R.id.tvFavoritePostalCode);
        }
    }
}
