package ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
     * @param onSuccess     Runnable to execute on successful transaction storage.
     * @param onFailure     Consumer to handle any exception that occurs.
     */
    public void storeTransaction(String ownerId, Transaction transaction, Runnable onSuccess, Consumer<Exception> onFailure) {
        DatabaseReference transactionRef = firebaseDatabase
                .getReference("owners")
                .child(ownerId)
                .child("transactions")
                .child(transaction.getTransactionId());

        transactionRef.setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.accept(e);
                });
    }
}
