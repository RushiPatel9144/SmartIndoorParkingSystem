package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
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
}
