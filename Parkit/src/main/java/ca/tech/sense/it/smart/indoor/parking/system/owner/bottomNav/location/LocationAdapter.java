package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final List<ParkingLocation> locations;
    private final OnItemClickListener itemClickListener;

    // Constructor for initializing the list and listener for item clicks
    public LocationAdapter(List<ParkingLocation> locations, OnItemClickListener itemClickListener) {
        this.locations = locations;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        ParkingLocation location = locations.get(position);
        holder.locationNameTextView.setText(location.getName());
        holder.locationAddressTextView.setText(location.getAddress());
        holder.locationPriceTextView.setText(String.format("Price: $%s", location.getPrice())); // Dynamically set price

        // Handle item click for opening slot list
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(location); // Pass clicked location
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    // ViewHolder class to hold each item
    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationNameTextView;
        TextView locationAddressTextView;
        TextView locationPriceTextView;

        public LocationViewHolder(View itemView) {
            super(itemView);

            // Find views
            locationNameTextView = itemView.findViewById(R.id.locationName);
            locationAddressTextView = itemView.findViewById(R.id.locationAddress);
            locationPriceTextView = itemView.findViewById(R.id.locationPrice);
        }
    }

    // Interface for handling item clicks (location)
    public interface OnItemClickListener {
        void onItemClick(ParkingLocation location);
    }
}
