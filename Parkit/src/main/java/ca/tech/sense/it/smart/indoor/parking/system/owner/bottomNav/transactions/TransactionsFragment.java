package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        initViews(rootView);
        setupRecyclerView();
        setupSwipeRefresh();

        String ownerId = getOwnerId();
        if (ownerId != null) {
            fetchTransactions(ownerId);
        }

        return rootView;
    }

    /**
     * Initializes Firebase instances.
     */
    private void initFirebase() {
        firebaseDatabase = FirebaseDatabaseSingleton.getInstance();
        mAuth = FirebaseAuthSingleton.getInstance();
    }

    /**
     * Initializes the views in the layout.
     */
    private void initViews(View rootView) {
        incomeTextView = rootView.findViewById(R.id.income);
        transactionsRecyclerView = rootView.findViewById(R.id.transactionRecyclerView);
        emptyStateLayout = rootView.findViewById(R.id.emptyStateLayout);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
    }

    /**
     * Sets up the RecyclerView and its adapter.
     */
    private void setupRecyclerView() {
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(transactionList);
        transactionsRecyclerView.setAdapter(transactionAdapter);
    }

    /**
     * Configures swipe-to-refresh functionality.
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String ownerId = getOwnerId();
            if (ownerId != null) {
                fetchTransactions(ownerId);
            }
        });
    }

    /**
     * Retrieves the current user's owner ID.
     */
    private String getOwnerId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        } else {
            return null;
        }
    }

    /**
     * Fetches transactions from Firebase and updates the UI.
     */
    private void fetchTransactions(String ownerId) {
        TransactionManager transactionManager = new TransactionManager(firebaseDatabase);
        transactionManager.retrieveTransactions(ownerId, this::onTransactionsFetched, this::onTransactionFetchFailed);
    }

    /**
     * Callback for successful transaction fetch.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void onTransactionsFetched(String ownerId, List<Transaction> transactions) {
        transactionList.clear();
        transactionList.addAll(transactions);
        sortTransactionsByTime();
        transactionAdapter.notifyDataSetChanged();

        updateEmptyState();
        updateTotalIncome(ownerId);
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Callback for transaction fetch failure.
     */
    private void onTransactionFetchFailed(Exception exception) {
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Updates the UI to reflect the empty state if there are no transactions.
     */
    private void updateEmptyState() {
        if (transactionList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            transactionsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            transactionsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates the total income and updates the text view.
     */
    @SuppressLint("DefaultLocale")
    private void updateTotalIncome(String ownerId) {
        double totalIncome = calculateTotalIncome();
        incomeTextView.setText(String.format("$ %.2f", totalIncome));

        // Optionally save the total income to Firebase
        new TransactionManager(firebaseDatabase).updateOwnerTotalIncome(ownerId, totalIncome);
    }

    /**
     * Calculates the total income from transactions.
     */
    private double calculateTotalIncome() {
        return transactionList.stream()
                .mapToDouble(transaction -> transaction.isRefunded() ? -transaction.getPrice() : transaction.getPrice())
                .sum();
    }

    /**
     * Sorts transactions by payment time in descending order.
     */
    private void sortTransactionsByTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        transactionList.sort((t1, t2) -> {
            LocalDateTime time1 = LocalDateTime.parse(t1.getPaymentTime(), formatter);
            LocalDateTime time2 = LocalDateTime.parse(t2.getPaymentTime(), formatter);
            return time2.compareTo(time1);
        });
    }
}
