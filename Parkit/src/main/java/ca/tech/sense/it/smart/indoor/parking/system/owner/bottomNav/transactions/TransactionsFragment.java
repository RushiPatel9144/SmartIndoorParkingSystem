package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.TransactionManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Transaction;

public class TransactionsFragment extends Fragment {

    private RecyclerView transactionsRecyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private TextView incomeTextView;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabaseSingleton.getInstance(); // Initialize Firebase database
        mAuth = FirebaseAuthSingleton.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);
        incomeTextView = rootView.findViewById(R.id.income);
        // Set up RecyclerView
        transactionsRecyclerView = rootView.findViewById(R.id.transactionRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter
        transactionAdapter = new TransactionAdapter(transactionList);
        transactionsRecyclerView.setAdapter(transactionAdapter);

        String ownerId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        retrieveTransactions(ownerId);

        return rootView;
    }

    private void retrieveTransactions(String ownerId) {
        // Calling the new retrieveTransactions method with BiConsumer for success and failure handling
        new TransactionManager(firebaseDatabase).retrieveTransactions(ownerId, new BiConsumer<String, List<Transaction>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void accept(String ownerId, List<Transaction> transactions) {
                // Success callback: update the transaction list and notify the adapter
                transactionList.clear();
                transactionList.addAll(transactions);
                transactionAdapter.notifyDataSetChanged();

                // Calculate the total price and update the toolbar
                double totalPrice = calculateTotalPrice();
                updateTextview(ownerId, totalPrice);
            }
        }, exception -> {});
    }
    private double calculateTotalPrice() {
        double totalPrice = 0.0;
        for (Transaction transaction : transactionList) {
            if (transaction.isRefunded()){
                totalPrice -= transaction.getPrice();
            } else totalPrice += transaction.getPrice();
        }
        return totalPrice;
    }

    @SuppressLint("DefaultLocale")
    private void updateTextview(String ownerId, Double price){
        new TransactionManager(firebaseDatabase).updateOwnerTotalIncome(ownerId, price);
        incomeTextView.setText(String.format("$ %.2f",price));
    }

}

