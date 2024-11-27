package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Objects;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;

public class ProfilePictureManager {

    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";
    private static final String FIELD = "profilePhotoUrl";
    private final Fragment fragment;
    private final FirebaseFirestore db;
    private final String collection;
    private final FirebaseAuth mAuth;

    public ProfilePictureManager(Fragment fragment, FirebaseFirestore db, String collection, FirebaseAuth mAuth) {
        this.fragment = fragment;
        this.db = db;
        this.collection = collection;
        this.mAuth = mAuth;
    }

    // Utility method to check if user is authenticated
    private boolean isUserAuthenticated() {
        return mAuth.getCurrentUser() == null;
    }

    private void saveProfilePhotoToFirestore(String photoUrl) {
        if (isUserAuthenticated()) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        DocumentReference userRef = FirestoreSingleton.getInstance()
                .collection(collection)
                .document(Objects.requireNonNull(mAuth.getUid()));

        userRef.update(FIELD, photoUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firestore", "Profile picture updated successfully.");
                    } else {
                        Log.e("Firestore", "Failed to update profile picture.");
                    }
                });
    }

    private void saveProfilePictureLocally(String photoUrl) {
        if (fragment.isAdded()) {
            SharedPreferences sharedPreferences = fragment.requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_PROFILE_PICTURE_URI, photoUrl);
            editor.apply();
        }
    }

    public void uploadProfilePicture(Uri imageUri) {
        if (isUserAuthenticated()) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_photos/" + mAuth.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        // On success, get the download URL for the uploaded image
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String photoUrl = uri.toString();
                                    saveProfilePhotoToFirestore(photoUrl);
                                    saveProfilePictureLocally(photoUrl);
                                })
                                .addOnFailureListener(e -> showSnackbar(R.string.profile_picture_upload_failed)))
                .addOnFailureListener(e -> showSnackbar(R.string.profile_picture_upload_failed));
    }

    private void saveProfilePictureUrlToFirestore(String downloadUrl, ImageView profilePicture) {
        if (isUserAuthenticated()) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        DocumentReference userRef = db.collection(collection).document(Objects.requireNonNull(mAuth.getUid()));

        // Update the user's profile with the new profile picture URL
        userRef.update(FIELD, downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar(R.string.profile_photo_updated);
                    loadProfilePicture(profilePicture);
                })
                .addOnFailureListener(e -> showSnackbar(R.string.update_failed));
    }

    public void saveProfilePicture(Uri uri, ImageView profilePicture) {
        if (isUserAuthenticated()) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile_photos/" + mAuth.getUid() + ".jpg");

        // Upload the image to Firebase Storage
        UploadTask uploadTask = storageReference.putFile(uri);
        uploadTask.addOnSuccessListener(taskSnapshot ->
                        storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> saveProfilePictureUrlToFirestore(downloadUrl.toString(), profilePicture)))
                .addOnFailureListener(e -> showSnackbar(R.string.update_failed));
    }

    public void loadProfilePicture(ImageView profilePicture) {
        if (isUserAuthenticated()) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        DocumentReference userRef = db.collection(collection).document(Objects.requireNonNull(mAuth.getUid()));

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String profilePictureUrl = document.getString(FIELD);
                    if (profilePictureUrl != null && fragment.isAdded()) {
                        saveProfilePictureLocally(profilePictureUrl);
                        Glide.with(fragment.requireContext())
                                .load(profilePictureUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)  // Optional: Use a default error image
                                .circleCrop()
                                .into(profilePicture);
                    } else {
                        profilePicture.setImageResource(R.mipmap.ic_launcher);
                    }
                } else {
                    showSnackbar(R.string.user_data_not_found);
                    profilePicture.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                showSnackbar(R.string.fetch_data_failed);
                profilePicture.setImageResource(R.mipmap.ic_launcher);
            }
        });
    }

    private void showSnackbar(int messageResId) {
        if (fragment.isAdded() && fragment.getView() != null) {
            Snackbar.make(fragment.requireView(), fragment.getString(messageResId), BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }
}
