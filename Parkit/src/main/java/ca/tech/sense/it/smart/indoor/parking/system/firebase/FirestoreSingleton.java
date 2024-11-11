package ca.tech.sense.it.smart.indoor.parking.system.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreSingleton {

    // Private constructor to prevent instantiation
    private FirestoreSingleton() {}

    // Bill Pugh Singleton: the instance is created when the inner class is loaded
    private static class FirestoreHolder {
        private static final FirebaseFirestore instance = FirebaseFirestore.getInstance();
    }

    // Method to get the FirebaseFirestore instance
    public static FirebaseFirestore getInstance() {
        return FirestoreHolder.instance;
    }
}

