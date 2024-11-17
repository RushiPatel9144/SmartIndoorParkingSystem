/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Favorites;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Favorites> favoriteLocationList; // List of addresses
    private DatabaseReference databaseRef;

    public FavoritesAdapter(List<Favorites> favoriteLocationList, DatabaseReference databaseRef) {
        this.favoriteLocationList = favoriteLocationList;
        this.databaseRef = databaseRef;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorites favorite = favoriteLocationList.get(position); // Use singular 'favorite'
        holder.tvFavoriteTitle.setText(favorite.getName()); // Set the name as the title
        holder.tvFavoriteAddress.setText(favorite.getAddress() + "\n" + favorite.getPostalCode());

        holder.btnRemoveFavorite.setOnClickListener(v -> {
            // Remove the favorite location from the database
            databaseRef.child(favorite.getId()).removeValue().addOnSuccessListener(aVoid -> {
                // Ensure the index is within bounds before removing the item
                if (position >= 0 && position < favoriteLocationList.size()) {
                    favoriteLocationList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, favoriteLocationList.size());
                    Toast.makeText(holder.itemView.getContext(), "Location removed from favorites", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(error -> {
                Toast.makeText(holder.itemView.getContext(), "Failed to remove the location" + error.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return favoriteLocationList.size();
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
