package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
        Transaction transaction = transactions.get(position);
        holder.parkingAddressTextView.setText(transaction.getParkingAddress());
        holder.priceTextView.setText(String.format("%s", transaction.getPrice()));
        holder.paymentTimeTextView.setText( transaction.getPaymentTime());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView parkingAddressTextView, priceTextView, paymentTimeTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingAddressTextView = itemView.findViewById(R.id.parkingAddressTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            paymentTimeTextView = itemView.findViewById(R.id.paymentTimeTextView);
        }
    }
}
