package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final List<ParkingLocation> locations;
    private final OnAddLocationClickListener addLocationClickListener;
    private final OnItemClickListener itemClickListener;

    // Constructor for initializing the list and listeners for "Add Location" and "Item Click"
    public LocationAdapter(List<ParkingLocation> locations,
                           OnAddLocationClickListener addLocationClickListener,
                           OnItemClickListener itemClickListener) {
        this.locations = locations;
        this.addLocationClickListener = addLocationClickListener;
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

        // Handle item click for location
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(location); // Pass the clicked location
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    // ViewHolder class to hold each item
    public class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationNameTextView;
        TextView locationAddressTextView;
        ImageButton addLocationButton;

        public LocationViewHolder(View itemView) {
            super(itemView);

            // Find views
            locationNameTextView = itemView.findViewById(R.id.locationName);
            locationAddressTextView = itemView.findViewById(R.id.locationAddress);
            addLocationButton = itemView.findViewById(R.id.addLocationButton);

            // Set click listener for the Add Location button
            addLocationButton.setOnClickListener(v -> {
                if (addLocationClickListener != null) {
                    addLocationClickListener.onAddLocationClick();
                }
            });
        }
    }

    // Interface for handling the "Add Location" click
    public interface OnAddLocationClickListener {
        void onAddLocationClick();
    }

    // Interface for handling item click (location)
    public interface OnItemClickListener {
        void onItemClick(ParkingLocation location);
    }
}
