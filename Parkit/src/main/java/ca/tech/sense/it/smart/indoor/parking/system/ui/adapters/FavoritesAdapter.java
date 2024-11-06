package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<String> favoriteLocationList; // List of addresses

    public FavoritesAdapter(List<String> favoriteLocationList) {
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
        String favoriteAddress = favoriteLocationList.get(position);
        holder.tvFavoriteTitle.setText("Favorite Location " + (position + 1));
        holder.tvFavoriteAddress.setText(favoriteAddress);
    }

    @Override
    public int getItemCount() {
        return favoriteLocationList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvFavoriteTitle;
        TextView tvFavoriteAddress;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFavoriteTitle = itemView.findViewById(R.id.tvFavoriteTitle);
            tvFavoriteAddress = itemView.findViewById(R.id.tvFavoriteAddress);
        }
    }
}
