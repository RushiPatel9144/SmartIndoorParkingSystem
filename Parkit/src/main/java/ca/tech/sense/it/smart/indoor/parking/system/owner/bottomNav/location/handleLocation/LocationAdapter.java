package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final List<ParkingLocation> locations;
    private final OnItemClickListener listener;

    // Constructor
    public LocationAdapter(List<ParkingLocation> locations, OnItemClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        ParkingLocation location = locations.get(position);

        // Bind the data to views
        holder.locationNameTextView.setText(location.getName());
        holder.locationAddressTextView.setText(location.getAddress());
        holder.locationPriceTextView.setText(String.format("Price: $%s", location.getPrice()));

        // Store the locationId in the ViewHolder
        holder.setLocationId(location.getId());
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

    // ViewHolder class
    public static class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView locationNameTextView;
        TextView locationAddressTextView;
        TextView locationPriceTextView;
        Button changePriceButton;
        Button addSlotsButton;
        private String locationId;

        public LocationViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            // Find views
            locationNameTextView = itemView.findViewById(R.id.locationName);
            locationAddressTextView = itemView.findViewById(R.id.locationAddress);
            locationPriceTextView = itemView.findViewById(R.id.locationPrice);
            changePriceButton = itemView.findViewById(R.id.changePriceButton);
            addSlotsButton = itemView.findViewById(R.id.addSlotsButton);

            // Button click listeners
            changePriceButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChangePriceClick(locationId, getBindingAdapterPosition());
                }
            });

            addSlotsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddSlotsClick(locationId, getBindingAdapterPosition());
                }
            });
        }

        public void setLocationId(String locationId) {
            this.locationId = locationId;
        }

        public String getLocationId() {
            return locationId;
        }
    }

    // Callback interface
    public interface OnItemClickListener {
        void onChangePriceClick(String locationId, int position);
        void onAddSlotsClick(String locationId, int position);
    }
}

