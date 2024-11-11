package ca.tech.sense.it.smart.indoor.parking.system.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreSingleton {

    // Private constructor to prevent instantiation
    private FirestoreSingleton() {}

    // This method provides the instance of FirebaseFirestore
    public static FirebaseFirestore getInstance() {
        return FirebaseFirestore.getInstance();
    }
}


