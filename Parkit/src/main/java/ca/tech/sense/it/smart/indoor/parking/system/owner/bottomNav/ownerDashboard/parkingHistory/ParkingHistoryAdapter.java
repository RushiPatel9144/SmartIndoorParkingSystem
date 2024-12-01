package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard.parkingHistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.ViewHolder> {
    private List<ParkingHistoryModel> parkingHistoryList;
    private Context context;

    public ParkingHistoryAdapter(Context context, List<ParkingHistoryModel> parkingHistoryList) {
        this.context = context;
        this.parkingHistoryList = parkingHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_lot_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingHistoryModel parkingHistory = parkingHistoryList.get(position);

        // Bind data to the views
        holder.locationName.setText(parkingHistory.getLocationName());
        holder.slotName.setText(parkingHistory.getSlotName());
        holder.paymentAmount.setText(parkingHistory.getPaymentAmount());
        holder.usageTime.setText(parkingHistory.getUsageTime());

        // Load user's profile picture (if available) into CircleImageView
        Glide.with(context)
                .load(parkingHistory.getUserProfilePicUrl())
                .into(holder.userProfilePic);
    }

    @Override
    public int getItemCount() {
        return parkingHistoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationName, slotName, paymentAmount, usageTime;
        CircleImageView userProfilePic;

        public ViewHolder(View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            slotName = itemView.findViewById(R.id.dashboardSlotName);
            paymentAmount = itemView.findViewById(R.id.dashboard_paymentAmount);
            usageTime = itemView.findViewById(R.id.dashbord_usageTime);
            userProfilePic = itemView.findViewById(R.id.dashboarduserProfilePic); // Your CircleImageView
        }
    }
}