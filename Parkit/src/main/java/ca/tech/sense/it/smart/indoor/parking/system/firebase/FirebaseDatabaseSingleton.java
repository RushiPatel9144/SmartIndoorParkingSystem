package ca.tech.sense.it.smart.indoor.parking.system.firebase;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseSingleton {

    // Private constructor to prevent instantiation
    private FirebaseDatabaseSingleton() {}

    // Bill Pugh Singleton: the instance is created when the inner class is loaded
    private static class FirebaseDatabaseHolder {
        private static final FirebaseDatabase instance = FirebaseDatabase.getInstance();

        static {
            // Enable offline persistence as soon as FirebaseDatabase instance is created
            instance.setPersistenceEnabled(true);
        }
    }

    // Method to get the FirebaseDatabase instance
    public static FirebaseDatabase getInstance() {
        return FirebaseDatabaseHolder.instance;
    }
}
