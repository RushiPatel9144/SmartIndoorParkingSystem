package ca.tech.sense.it.smart.indoor.parking.system.userUi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.text.MessageFormat;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Favorites;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final List<Favorites> favoriteLocationList;
    private final DatabaseReference databaseRef;
    private final OnItemClickListener onItemClickListener;
    private final Context context;

    // Interface for handling item click events
    public interface OnItemClickListener {
        void onItemClick(Favorites favorite);
    }

    // Constructor for adapter initialization
    public FavoritesAdapter(Context context, List<Favorites> favoriteLocationList, DatabaseReference databaseRef, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.favoriteLocationList = favoriteLocationList;
        this.databaseRef = databaseRef;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create a ViewHolder instance
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        // Get the favorite location item at the current position
        Favorites favorite = favoriteLocationList.get(position);

        // Set the name and address of the favorite location in the UI
        holder.tvFavoriteTitle.setText(favorite.getName());
        holder.tvFavoriteAddress.setText(MessageFormat.format("{0}\n{1}", favorite.getAddress(), favorite.getPostalCode()));

        // Handle item click to notify the listener
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(favorite));

        // Handle remove button click to delete the favorite location
        holder.btnRemoveFavorite.setOnClickListener(v -> removeFavorite(holder, favorite, position));
    }

    @Override
    public int getItemCount() {
        return favoriteLocationList.size();
    }

    // This method is responsible for removing a favorite from the list and database
    private void removeFavorite(FavoriteViewHolder holder, Favorites favorite, int position) {
        databaseRef.child(favorite.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (position >= 0 && position < favoriteLocationList.size()) {
                        // Remove the item from the list
                        favoriteLocationList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, favoriteLocationList.size());
                        if (context != null){
                            showToast(holder.itemView, context.getString(R.string.location_removed_from_favorites));
                        }
                    }
                })
                .addOnFailureListener(error -> showToast(holder.itemView, context.getString(R.string.failed_to_remove_the_location) + error.getMessage()));
    }

    private void showToast(View view, String message) {
        Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvFavoriteTitle;
        TextView tvFavoriteAddress;
        ImageButton btnRemoveFavorite;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFavoriteTitle = itemView.findViewById(R.id.tvFavoriteTitle);
            tvFavoriteAddress = itemView.findViewById(R.id.tvFavoriteAddress);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
