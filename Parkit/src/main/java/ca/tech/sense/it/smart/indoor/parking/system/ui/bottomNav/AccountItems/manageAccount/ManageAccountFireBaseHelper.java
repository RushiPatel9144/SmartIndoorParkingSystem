package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;

public class ManageAccountFireBaseHelper {

    public static void updatePhoneNumberInFirestore(String phoneNumber, FirebaseUser currentUser, FirebaseFirestore db, String collection, TextView phoneNumberTextView, View view) {
        if (currentUser != null) {
            // Get reference to the user's document in Firestore
            DocumentReference userRef = db.collection(collection).document(currentUser.getUid());

            // Update the 'phone' field in Firestore
            userRef.update("phone", phoneNumber)
                    .addOnSuccessListener(aVoid -> {
                        phoneNumberTextView.setText(phoneNumber);  // Update UI with the new phone number
                        showSnackbar(R.string.phone_number_updated,view);  // Show success message
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Failed to update phone number", e);
                        showSnackbar(R.string.phone_number_update_failed,view);  // Show failure message
                    });
        } else {
            showSnackbar(R.string.user_not_authenticated,view);  // If user is not authenticated
        }
    }

    public static void fetchUserDetailsFromFirestore(String uid, FirebaseFirestore db, String collection, TextView nameTextView, TextView phoneNumberTextView, View view, SessionManager sessionManager) {
        // Fetch data from the SessionManager
        String firstName = sessionManager.getCurrentUser().getFirstName();
        String lastName = sessionManager.getCurrentUser().getLastName();
        String phoneNumber = sessionManager.getCurrentUser().getPhone();

        // Update the UI with cached data
        if (firstName != null && lastName != null) {
            nameTextView.setText(firstName + " " + lastName);
        } else {
            nameTextView.setText("USer name not found");
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            phoneNumberTextView.setText(phoneNumber);
        } else {
            phoneNumberTextView.setText(view.getContext().getString(R.string.add_phone_number));
        }
    }

    public static void loadProfilePicture(FirebaseUser currentUser, FirebaseFirestore db, String collection, ImageView profilePicture, View view, Context context) {
        if (currentUser != null) {
            DocumentReference userRef = db.collection(collection).document(currentUser.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String profilePictureUrl = document.getString("profilePhotoUrl");
                    if (profilePictureUrl != null) {
                        Glide.with(context).load(profilePictureUrl).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(profilePicture);
                    } else {
                        profilePicture.setImageResource(R.mipmap.ic_launcher);
                    }
                } else {
                    showSnackbar(R.string.fetch_data_failed,view);
                    profilePicture.setImageResource(R.mipmap.ic_launcher);
                }
            });
        }
    }

    public static void saveProfilePicture(Uri uri, FirebaseUser currentUser, View view, FirebaseFirestore db, String collection, ImageView profilePicture, Context context) {
        if (currentUser != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child("profile_photos/" + currentUser.getUid() + ".jpg");
            storageReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                            .addOnSuccessListener(downloadUrl -> saveProfilePictureUrlToFirestore(downloadUrl.toString(), currentUser, db, collection, view, context, profilePicture   ))
                            .addOnFailureListener(e -> showSnackbar(R.string.update_failed,view)))
                    .addOnFailureListener(e -> showSnackbar(R.string.upload_failed,view));
        }
    }

    private static void saveProfilePictureUrlToFirestore(String downloadUrl, FirebaseUser currentUser, FirebaseFirestore db, String collection, View view, Context context, ImageView profilePicture) {
        if (currentUser != null) {
            DocumentReference userRef = db.collection(collection).document(currentUser.getUid());
            userRef.update("profilePhotoUrl", downloadUrl)
                    .addOnSuccessListener(aVoid -> {
                        showSnackbar(R.string.profile_photo_updated,view);
                        ManageAccountFireBaseHelper.loadProfilePicture(currentUser, db, collection, profilePicture, view, context);
                    })
                    .addOnFailureListener(e -> showSnackbar(R.string.update_failed,view));
        }
    }

    public static void showSnackbar(int resId, View view) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
    }
}
