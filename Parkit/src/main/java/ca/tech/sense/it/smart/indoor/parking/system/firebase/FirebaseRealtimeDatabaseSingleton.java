package ca.tech.sense.it.smart.indoor.parking.system.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRealtimeDatabaseSingleton {

    // Private constructor to prevent instantiation
    private FirebaseRealtimeDatabaseSingleton() {}

    // Bill Pugh Singleton: the instance is created when the inner class is loaded
    private static class FirebaseRealtimeDatabaseHolder {
        private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        static {
            // Enable offline persistence for the Firebase database
            firebaseDatabase.setPersistenceEnabled(true);
        }
        // Get the reference to the Firebase Realtime Database
        private static final DatabaseReference instance = firebaseDatabase.getReference();
    }

    // Method to get the DatabaseReference instance
    public static DatabaseReference getInstance() {
        return FirebaseRealtimeDatabaseHolder.instance;
    }
}
