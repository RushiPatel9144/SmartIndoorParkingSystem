package ca.tech.sense.it.smart.indoor.parking.system.firebase;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthSingleton {

    private FirebaseAuthSingleton() {}

    // Bill Pugh Singleton: the instance is created when the inner class is loaded
    private static class FirebaseAuthHolder {
        private static final FirebaseAuth instance = FirebaseAuth.getInstance();
    }

    // Method to get the FirebaseAuth instance
    public static FirebaseAuth getInstance() {
        return FirebaseAuthHolder.instance;
    }
}


