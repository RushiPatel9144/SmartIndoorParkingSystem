package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation;

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
    private final OnItemClickListener listener;

    // Constructor for initializing the list and listener
    public LocationAdapter(List<ParkingLocation> locations, OnItemClickListener listener) {
        this.locations = locations;
        this.listener = listener;
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

        // Bind the data to views
        holder.locationNameTextView.setText(location.getName());
        holder.locationAddressTextView.setText(location.getAddress());
        holder.locationPriceTextView.setText(String.format("Price: $%s", location.getPrice()));

        // Store the locationId in the ViewHolder
        holder.setLocationId(location.getId()); // Set the locationId

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(location, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    // Method to remove an item
    public void removeItem(int position) {
        locations.remove(position);
        notifyItemRemoved(position);
    }

    // ViewHolder class to hold each item
    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationNameTextView;
        TextView locationAddressTextView;
        TextView locationPriceTextView;
        private String locationId; // Store the locationId

        public LocationViewHolder(View itemView) {
            super(itemView);
            // Find views
            locationNameTextView = itemView.findViewById(R.id.locationName);
            locationAddressTextView = itemView.findViewById(R.id.locationAddress);
            locationPriceTextView = itemView.findViewById(R.id.locationPrice);
        }

        public void setLocationId(String locationId) {
            this.locationId = locationId; // Set the locationId
        }

        public String getLocationId() {
            return locationId; // Getter for locationId
        }
    }

    // Callback interface for item clicks
    public interface OnItemClickListener {
        void onItemClick(ParkingLocation location, int position);
    }
}
