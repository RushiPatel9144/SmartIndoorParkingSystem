package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AuthUtils;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ImageCropActivity;
import ca.tech.sense.it.smart.indoor.parking.system.utility.UserCheckHelper;

public class ManageAccountFragment extends Fragment {

    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";
    private static final String FIELD = "profilePhotoUrl";
    private static final String COLLECTION_USER = "users";
    private static final String COLLECTION_OWNER = "owners";
    private String collection = "users";

    private ImageView profilePicture;
    private TextView nameTextView;
    private TextView contactDetailsTextView;
    private TextView phoneNumberTextView;
    private LinearLayout manageProfilePicture;
    private LinearLayout managePassword;
    private LinearLayout manageEmail;
    private LinearLayout managePhoneNumber;
    private View rootView;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private LinearLayout linearLayout;
    private FrameLayout progressFrame;
    private ProfileEditManager profileEditManager;
    private FirebaseAuth mAuth;
    // Executor service to schedule tasks
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

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

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (Boolean.TRUE.equals(isGranted)) {
                    openGallery();
                } else {
                    showSnackbar(R.string.permission_denied_to_read_external_storage);
                }
            });

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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_account, container, false);
        bindViews(rootView);
        mAuth = FirebaseAuthSingleton.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirestoreSingleton.getInstance();
        checkUserType();
        scheduleCheckUserType();
        profileEditManager = new ProfileEditManager(this, db, collection, currentUser.getUid());
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupProfilePictureButton();
        setupButton();
    }

    private void bindViews(View view) {
        profilePicture = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberManage);
        manageProfilePicture = view.findViewById(R.id.manageProfilePic);
        managePassword = view.findViewById(R.id.managePassword);
        manageEmail = view.findViewById(R.id.manageEmail);
        managePhoneNumber = view.findViewById(R.id.managePhoneNumber);
        linearLayout = view.findViewById(R.id.manage_account);
        progressFrame = view.findViewById(R.id.progressFrame);
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getString(R.string.image1));
        imagePickerLauncher.launch(intent);
    }

    public void loadProfilePicture() {
        if (currentUser == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        DocumentReference userRef = db.collection(collection).document(currentUser.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String profilePictureUrl = document.getString("profilePhotoUrl");

                    if (profilePictureUrl != null) {
                        Glide.with(requireContext())
                                .load(profilePictureUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
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

    private void saveProfilePicture(Uri uri) {
        if (currentUser == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile_photos/" + currentUser.getUid() + ".jpg");
        // Upload the image to Firebase Storage
        UploadTask uploadTask = storageReference.putFile(uri);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> saveProfilePictureUrlToFirestore(downloadUrl.toString()))).addOnFailureListener(e -> showSnackbar(R.string.update_failed));
    }

    private void saveProfilePictureUrlToFirestore(String downloadUrl) {
        if (currentUser == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }
        DocumentReference userRef = db.collection(collection).document(currentUser.getUid());
        // UpdLate the user's profile with the new profile picture UR
        userRef.update(FIELD, downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar(R.string.profile_photo_updated);
                    loadProfilePicture();
                })
                .addOnFailureListener(e -> showSnackbar(R.string.update_failed));
    }

    private void uploadProfilePicture(Uri imageUri) {
        if (currentUser == null) {
            showSnackbar(R.string.user_not_found);
            return;
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_photos/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        // On success, get the download URL for the uploaded image
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String photoUrl = uri.toString();
                                    saveProfilePhotoToFirestore(photoUrl);
                                    // Optionally, save locally for session usage
                                    saveProfilePictureLocally(photoUrl);
                                    Log.d(FIELD, getString(R.string.upload_successful_url) + photoUrl);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Log.e(FIELD, getString(R.string.upload_failed), e);
                                    showSnackbar(R.string.profile_picture_upload_failed);
                                }))
                .addOnFailureListener(e -> {
                    // Handle failure in uploading image
                    Log.e(FIELD, getString(R.string.failed_to_upload_image), e);
                    showSnackbar(R.string.profile_picture_upload_failed);
                });
    }

    private void saveProfilePictureLocally(String photoUrl) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_PICTURE_URI, photoUrl);
        editor.apply();
    }

    private void saveProfilePhotoToFirestore(String photoUrl) {
        if (currentUser != null) {
            DocumentReference userRef = FirestoreSingleton.getInstance()
                    .collection(collection)
                    .document(currentUser.getUid());

            userRef.update("profilePhotoUrl", photoUrl)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firestore", "Profile picture updated successfully.");
                        } else {
                            Log.e("Firestore", "Failed to update profile picture.");
                        }
                    });
        }
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void scheduleCheckUserType() {
        scheduledExecutorService.schedule(() -> {
            profileEditManager.fetchUserDetailsFromFirestore(mAuth, contactDetailsTextView,nameTextView,phoneNumberTextView);
            loadProfilePicture();
        }, 1, TimeUnit.SECONDS);
    }

    public void setupButton(){
        managePassword.setOnClickListener(v -> AuthUtils.showResetPasswordDialog(requireContext(), FirebaseAuth.getInstance()));
        manageEmail.setOnClickListener(view -> profileEditManager.manageEmail());
        managePhoneNumber.setOnClickListener(view -> profileEditManager.managePhoneNumber());
    }

    private void checkUserType() {
        SessionManager sessionManager = new SessionManager(requireContext());
        if (Objects.equals(sessionManager.getUserType(), "owner")) {
            collection = COLLECTION_OWNER;
        } else {
            collection = COLLECTION_USER;
        }
    }

    private void showSnackbar(int messageResId) {
        Snackbar.make(rootView, messageResId, -1).show();
    }
}

