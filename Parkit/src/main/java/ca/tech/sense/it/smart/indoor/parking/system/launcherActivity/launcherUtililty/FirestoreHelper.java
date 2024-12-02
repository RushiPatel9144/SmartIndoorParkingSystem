package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;

public class FirestoreHelper {
    private FirestoreHelper(){}

    private static final FirebaseFirestore fireStore = FirestoreSingleton.getInstance();

    public static void saveOwnerToFirestore(Context context, String userID, String fName, String lName, String email, String phoneNumber) {
        DocumentReference ownerRef = fireStore.collection("owners").document(userID);
        ownerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Owner localOwner = new Owner(userID, fName, lName, email, phoneNumber, null);
                ownerRef.set(localOwner)
                        .addOnSuccessListener(aVoid -> NavigationHelper.navigateToOwnerDashboard((AppCompatActivity) context))
                        .addOnFailureListener(e -> ToastHelper.showToast(context, context.getString(R.string.failed_to_save_owner_data)));
            }
        }).addOnFailureListener(e -> ToastHelper.showToast(context, context.getString(R.string.failed_to_check_owner_data)));
    }

    public static void saveUserToFirestore(Context context, String userID, String fName, String lName, String email, String phoneNumber) {
        DocumentReference userRef = fireStore.collection("users").document(userID);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                User localUser = new User(userID, fName, lName, email, phoneNumber, null);
                userRef.set(localUser)
                        .addOnSuccessListener(aVoid -> NavigationHelper.navigateToMainActivity((AppCompatActivity) context))
                        .addOnFailureListener(e -> ToastHelper.showToast(context, context.getString(R.string.failed_to_save_user_data)));
            }
        }).addOnFailureListener(e -> ToastHelper.showToast(context, context.getString(R.string.failed_to_check_user_data)));
    }



}
