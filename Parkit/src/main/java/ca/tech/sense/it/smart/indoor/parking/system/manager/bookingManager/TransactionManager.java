package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Transaction;

public class TransactionManager {

    private final FirebaseDatabase firebaseDatabase;

    public TransactionManager(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    /**
     * Stores transaction details in the Firebase Realtime Database under the owner node.
     *
     * @param ownerId       The ID of the owner under whom the transaction will be stored.
     * @param transaction   The transaction object to store.
     */
    public void storeTransaction(String ownerId, Transaction transaction) {
        DatabaseReference transactionRef = firebaseDatabase
                .getReference("owners")
                .child(ownerId)
                .child("transactions")
                .child(transaction.getTransactionId());

        transactionRef.setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    //
                })
                .addOnFailureListener(e -> {
                    //
                });
    }

    /**
     * Retrieves all transactions for a specific owner from the Firebase Realtime Database.
     *
     * @param ownerId   The ID of the owner whose transactions are to be retrieved.
     * @param onSuccess BiConsumer to handle the list of transactions and the owner ID on success.
     * @param onFailure Consumer to handle any exception that occurs during retrieval.
     */
    public void retrieveTransactions(String ownerId, BiConsumer<String, List<Transaction>> onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference transactionsRef = firebaseDatabase
                .getReference("owners")
                .child(ownerId)
                .child("transactions");

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Transaction> transactions = new ArrayList<>();
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }
                onSuccess.accept(ownerId, transactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onFailure != null) {
                    onFailure.accept(error.toException());
                }
            }
        });
    }
    /**
     * Updates the total income of the owner in Firebase Realtime Database.
     *
     * @param ownerId       The ID of the owner whose total income will be updated.
     * @param totalIncome  The income amount to be added to the total income.
     */
    public void updateOwnerTotalIncome(String ownerId, double totalIncome) {
        DatabaseReference ownerRef = firebaseDatabase
                .getReference("owners")
                .child(ownerId)
                .child("totalIncome");

        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String income = String.format(Locale.getDefault(), "%.2f", totalIncome);
                ownerRef.setValue(income)
                        .addOnSuccessListener(aVoid ->
                            Log.d("IncomeUpdate", "Total income updated successfully."))
                        .addOnFailureListener(e ->
                            Log.e("IncomeUpdate", "Failed to update total income.", e));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("IncomeUpdate", "Error fetching total income data: " + error.getMessage());
            }
        });
    }

    /**
     * Retrieves the total income of the owner from Firebase Realtime Database.
     *
     * @param ownerId   The ID of the owner whose total income will be retrieved.
     * @param callback  The callback to handle the result of the fetch operation.
     */
    public void retrieveOwnerTotalIncome(String ownerId, final FetchIncomeCallback callback) {
        DatabaseReference ownerRef = firebaseDatabase
                .getReference("owners")
                .child(ownerId)
                .child("totalIncome");

        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double totalIncome = snapshot.getValue(Double.class);
                    callback.onSuccess(totalIncome);
                } else {
                    callback.onFailure("No income data found for owner ID: " + ownerId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Error fetching total income data: " + error.getMessage());
            }
        });
    }

    /**
     * Callback interface to handle the success and failure of income retrieval.
     */
    public interface FetchIncomeCallback {
        void onSuccess(Double totalIncome);  // Callback for successful retrieval
        void onFailure(String errorMessage); // Callback for failure or no data found
    }

}
