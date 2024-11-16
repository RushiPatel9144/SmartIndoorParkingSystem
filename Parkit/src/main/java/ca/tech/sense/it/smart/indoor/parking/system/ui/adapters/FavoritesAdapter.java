/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Favorites;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Favorites> favoriteLocationList; // List of addresses

    public FavoritesAdapter(List<Favorites> favoriteLocationList) {
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
        Favorites favorite = favoriteLocationList.get(position);
        holder.tvFavoriteTitle.setText(favorite.getName());
        holder.tvFavoriteAddress.setText(favorite.getAddress() + "\n" + favorite.getPostalCode());
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
