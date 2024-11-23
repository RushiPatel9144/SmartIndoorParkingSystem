package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleSlot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.MessageFormat;
import java.util.List;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private final List<ParkingSlot> parkingSlots;

    public SlotAdapter(List<ParkingSlot> parkingSlots) {
        this.parkingSlots = parkingSlots;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slot_item, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        ParkingSlot slot = parkingSlots.get(position);
        holder.slotIdTextView.setText(String.format("Slot ID: %s", slot.getId()));
        holder.slotStatusTextView.setText(String.format("Status: %s", slot.getHourlyStatus()));

        if (slot.getSensor() != null) {
            holder.sensorInfoTextView.setText(MessageFormat.format("Sensor: {0} (Battery: {1}%)", slot.getSensor().getType(), slot.getSensor().getBatteryLevel()));
        }
    }

    @Override
    public int getItemCount() {
        return parkingSlots.size();
    }

    public static class SlotViewHolder extends RecyclerView.ViewHolder {

        TextView slotIdTextView;
        TextView slotStatusTextView;
        TextView sensorInfoTextView;

        public SlotViewHolder(View itemView) {
            super(itemView);
            slotIdTextView = itemView.findViewById(R.id.slotIdTextView);
            slotStatusTextView = itemView.findViewById(R.id.slotStatusTextView);
            sensorInfoTextView = itemView.findViewById(R.id.sensorInfoTextView);
        }
    }
}
