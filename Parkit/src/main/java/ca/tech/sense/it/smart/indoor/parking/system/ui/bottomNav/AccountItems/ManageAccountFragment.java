package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AuthUtils;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ImageCropActivity;

public class ManageAccountFragment extends Fragment {

    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";

    private ImageView profilePicture;
    private TextView nameTextView;
    private TextView contactDetailsTextView;
    private TextView phoneNumberTextView;
    private LinearLayout manageProfilePicture;
    private LinearLayout managePassword;
    private LinearLayout manageEmail;
    private View rootView;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        cropImage(selectedImageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> imageCropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String croppedImageUriString = result.getData().getStringExtra(getString(R.string.croppedimageuri));
                    if (croppedImageUriString != null) {
                        Uri croppedImageUri = Uri.parse(croppedImageUriString);
                        loadCroppedImage(croppedImageUri);
                        saveProfilePicture(croppedImageUri);
                        uploadProfilePicture(croppedImageUri); // Upload new profile picture to Firebase
                    }
                }
            }
    );

    private void loadCroppedImage(Uri imageUri) {
        try {
            Bitmap bitmap;
            ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), imageUri);
            bitmap = ImageDecoder.decodeBitmap(source);
            profilePicture.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            profilePicture.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    showSnackbar(R.string.permission_denied_to_read_external_storage);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_account, container, false);
        bindViews(rootView);

        fetchUserInfo();
        loadProfilePicture();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupProfilePictureButton();
        setupPasswordResetButton();
        manageEmail();
    }

    private void bindViews(View view) {
        profilePicture = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberManage);
        manageProfilePicture = view.findViewById(R.id.manageProfilePic);
        managePassword = view.findViewById(R.id.managePassword);
        manageEmail = view.findViewById(R.id.manageEmail);
    }

    private void setupProfilePictureButton() {
        manageProfilePicture.setOnClickListener(v -> {
            if (isPermissionGranted()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void cropImage(Uri imageUri) {
        Intent intent = new Intent(getActivity(), ImageCropActivity.class);
        intent.putExtra(getString(R.string.imageuri), imageUri);
        imageCropLauncher.launch(intent);
    }


    private void fetchUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String name = user.getDisplayName();
            String phoneNumber = user.getPhoneNumber();
            if (email != null) {
                contactDetailsTextView.setText(email);
            }
            if (name != null){
                nameTextView.setText(name);
            }
            if (phoneNumber != null){
                phoneNumberTextView.setText(phoneNumber);
            }
            if(name == null && phoneNumber == null){
                String uid = user.getUid();
                fetchUserDetailsFromFirestore(uid);
            }
        } else {
            showSnackbar(R.string.user_not_authenticated);
        }
    }

    private void fetchUserDetailsFromFirestore(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");
                    String phoneNumber = document.getString("phone");

                    if (firstName != null) {
                        nameTextView.setText(firstName);
                    }
                    if (lastName != null) {
                        nameTextView.append(" " + lastName);
                    }
                    if (phoneNumber != null) {
                        phoneNumberTextView.setText(phoneNumber);
                    } else {
                        phoneNumberTextView.setText(R.string.add_phone_number);
                    }
                } else {
                    showSnackbar(R.string.user_data_not_found);
                }
            } else {
                Log.e("TAG", "Firestore fetch failed: " + task.getException());
                showSnackbar(R.string.fetch_data_failed);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getString(R.string.image1));
        imagePickerLauncher.launch(intent);
    }

    private void loadProfilePicture() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String profilePictureUrl = document.getString("profilePictureUrl");

                    if (profilePictureUrl != null) {
                        // Load the image into ImageView using Glide
                        Glide.with(requireContext())
                                .load(profilePictureUrl)
                                .placeholder(R.mipmap.ic_launcher)  // Placeholder while loading
                                .into(profilePicture);
                    } else {
                        // If there is no profile picture URL, use the default image
                        profilePicture.setImageResource(R.mipmap.ic_launcher);  // Default image
                    }
                } else {
                    showSnackbar(R.string.user_data_not_found);
                    profilePicture.setImageResource(R.mipmap.ic_launcher);  // Default image
                }
            } else {
                showSnackbar(R.string.fetch_data_failed);
                Log.e("ManageAccountFragment", "Failed to fetch user data: " + task.getException());
                profilePicture.setImageResource(R.mipmap.ic_launcher);  // Default image
            }
        });
    }

    private void saveProfilePicture(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile_pictures/" + user.getUid() + ".jpg");

        // Upload the image to Firebase Storage
        UploadTask uploadTask = storageReference.putFile(uri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                saveProfilePictureUrlToFirestore(downloadUrl.toString());
            });
        }).addOnFailureListener(e -> {
            showSnackbar(R.string.update_failed);
        });
    }

    private void saveProfilePictureUrlToFirestore(String downloadUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());
        // UpdLate the user's profile with the new profile picture UR
        userRef.update("profilePictureUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar(R.string.profile_photo_updated);
                    loadProfilePicture();
                })
                .addOnFailureListener(e -> {
                    showSnackbar(R.string.update_failed);
                });
    }

    private void uploadProfilePicture(Uri imageUri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showSnackbar(R.string.user_not_found);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_photos/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // On success, get the download URL for the uploaded image
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String photoUrl = uri.toString();
                                saveProfilePhotoToFirestore(photoUrl);
                                // Optionally, save locally for session usage
                                saveProfilePictureLocally(photoUrl);
                                Log.d("ProfilePicture", "Upload successful. URL: " + photoUrl);
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                Log.e("ProfilePicture", "Upload failed", e);
                                showSnackbar(R.string.profile_picture_upload_failed);
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure in uploading image
                    Log.e("ProfilePicture", "Failed to upload image", e);
                    showSnackbar(R.string.profile_picture_upload_failed);
                });
    }

    private void saveProfilePictureLocally(String photoUrl) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_PICTURE_URI, photoUrl);  // Save the Firebase URL as the key
        editor.apply();
    }

    private void saveProfilePhotoToFirestore(String photoUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid());

            userRef.update("profilePicture", photoUrl)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firestore", "Profile picture updated successfully.");
                        } else {
                            Log.e("Firestore", "Failed to update profile picture.");
                        }
                    });
        }
    }

    private void showSnackbar(int messageResId) {
        Snackbar.make(rootView, messageResId, Snackbar.LENGTH_SHORT).show();
    }


    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void setupPasswordResetButton() {
        managePassword.setOnClickListener(v -> AuthUtils.showResetPasswordDialog(requireContext(), FirebaseAuth.getInstance()));
    }

    private void manageEmail() {
        manageEmail.setOnClickListener(v -> {
            DialogUtil.showMessageDialog(
                    requireContext(),
                    getString(R.string.email_update_unavailable),
                    getString(R.string.changing_your_email_address_is_currently_not_permitted_please_reach_out_to_our_support_team_for_further_assistance),
                    getString(R.string.help),
                    new DialogUtil.DialogCallback() {
                        @Override
                        public void onConfirm() {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.flFragment, new HelpFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                        @Override
                        public void onCancel() {
                            // Do nothing, just close the dialog
                        }
                    }
            );
        });
    }
}
