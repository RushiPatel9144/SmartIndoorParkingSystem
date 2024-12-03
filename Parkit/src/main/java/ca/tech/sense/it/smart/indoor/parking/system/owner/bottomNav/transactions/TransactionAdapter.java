package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;

    }
    
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        String sign = "+";
        Transaction transaction = transactions.get(position);
        holder.parkingAddressTextView.setText(transaction.getParkingAddress());
        Context context = holder.itemView.getContext();
        if (!transaction.isRefunded()){
            sign = "+";
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.holo_green_dark));
        } else {
            sign = "-";
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
        holder.priceTextView.setText(String.format(Locale.getDefault(), "%s CAD$ %.2f", sign , transaction.getPrice()));
        holder.paymentTimeTextView.setText( transaction.getPaymentTime());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView parkingAddressTextView;
        TextView priceTextView;
        TextView paymentTimeTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingAddressTextView = itemView.findViewById(R.id.parkingAddressTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            paymentTimeTextView = itemView.findViewById(R.id.paymentTimeTextView);
        }
    }
}
