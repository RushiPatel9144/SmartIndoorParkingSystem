package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;

public class FirestoreHelper {

    private static FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

    public static void saveOwnerToFirestore(Context context, String userID, String fName, String lName, String email, String phoneNumber) {
        DocumentReference ownerRef = fireStore.collection("owners").document(userID);
        ownerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Owner localOwner = new Owner(userID, fName, lName, email, phoneNumber, null);
                ownerRef.set(localOwner)
                        .addOnSuccessListener(aVoid -> NavigationHelper.navigateToOwnerDashboard((AppCompatActivity) context))
                        .addOnFailureListener(e -> ToastHelper.showToast(context, "Failed to save owner data."));
            }
        }).addOnFailureListener(e -> ToastHelper.showToast(context, "Failed to check owner data."));
    }

    public static void saveUserToFirestore(Context context, String userID, String fName, String lName, String email, String phoneNumber) {
        DocumentReference userRef = fireStore.collection("users").document(userID);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                User localUser = new User(userID, fName, lName, email, phoneNumber, null);
                userRef.set(localUser)
                        .addOnSuccessListener(aVoid -> NavigationHelper.navigateToMainActivity((AppCompatActivity) context))
                        .addOnFailureListener(e -> ToastHelper.showToast(context, "Failed to save user data."));
            }
        }).addOnFailureListener(e -> ToastHelper.showToast(context, "Failed to check user data."));
    }



}
