package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private ArrayList<String> accountOptions;
    private OnItemClickListener onItemClickListener;

    public AccountAdapter(ArrayList<String> accountOptions, OnItemClickListener onItemClickListener) {
        this.accountOptions = accountOptions;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your custom layout for each account option
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(accountOptions.get(position));

        // Set icons based on position or account type
        switch (position) {
            case 0:
                holder.icon.setImageResource(R.drawable.ic_launcher_foreground);
                holder.icon.getLayoutParams().width = 232; //dp
                holder.icon.getLayoutParams().height = 232; //dp
                holder.textView.setTextSize(25); // Increase text size
                holder.textView.setTypeface(null, Typeface.BOLD); // Make it bold
                break;
            case 1:
                holder.icon.setImageResource(R.drawable.nav_settings);
                break;
            case 2:
                holder.icon.setImageResource(R.drawable.help);
                break;
            case 3:
                holder.icon.setImageResource(R.drawable.rate);
                break;
            case 4:
                holder.icon.setImageResource(R.drawable.notifications);
                break;
            case 5:
                holder.icon.setImageResource(R.drawable.private_policy);
                break;
            case 6:
                holder.icon.setImageResource(R.drawable.terms_of_use);
                break;
            case 7:
                holder.icon.setImageResource(R.drawable.nav_logout);
                break;
            default:
                holder.icon.setImageResource(R.drawable.ic_launcher_foreground); // Default icon
        }

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return accountOptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView); // Ensure this matches your XML
            icon = itemView.findViewById(R.id.item_icon); // Ensure this matches your XML
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
