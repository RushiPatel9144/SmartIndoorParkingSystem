package ca.tech.sense.it.smart.indoor.parking.system.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRealtimeDatabaseSingleton {

    // Private constructor to prevent instantiation
    private FirebaseRealtimeDatabaseSingleton() {}

    // Bill Pugh Singleton: the instance is created when the inner class is loaded
    private static class FirebaseRealtimeDatabaseHolder {
        private static final DatabaseReference instance = FirebaseDatabase.getInstance().getReference();
    }

    // Method to get the DatabaseReference instance
    public static DatabaseReference getInstance() {
        return FirebaseRealtimeDatabaseHolder.instance;
    }
}
