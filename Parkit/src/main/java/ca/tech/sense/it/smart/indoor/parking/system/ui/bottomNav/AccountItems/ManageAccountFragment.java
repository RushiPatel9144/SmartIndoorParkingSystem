package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import static ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LoginActivity.RC_SIGN_IN;

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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.UserManager;
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
                    String croppedImageUriString = result.getData().getStringExtra("croppedImageUri");
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
        handleGoogleSignIn();
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
        intent.putExtra("imageUri", imageUri);
        imageCropLauncher.launch(intent);
    }

    private void fetchUserInfo() {
        User user = UserManager.getInstance().getCurrentUser();

        if (user != null) {
            // Populate UI with cached user data
            String email = user.getEmail();
            if (email != null) {
                contactDetailsTextView.setText(email);
            }

            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            String phone = user.getPhone();

            if (firstName != null) {
                nameTextView.setText(firstName);
            }
            if (lastName != null) {
                nameTextView.append(" " + lastName);
            }
            if (phone != null) {
                phoneNumberTextView.setText(phone);
            } else {
                phoneNumberTextView.setText(R.string.add_phone_number);
            }

            Log.d("Activity", "User data retrieved from cache.");
        } else {
            Log.d("Activity", "User data not available in cache.");
            showSnackbar(R.string.user_data_not_found);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadProfilePicture() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }

        // Fetch the user's profile picture URL from Firestore
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
//        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(KEY_PROFILE_PICTURE_URI, uri.toString());
//        editor.apply(); // Apply the changes
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }

        // Get a reference to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile_pictures/" + user.getUid() + ".jpg");

        // Upload the image to Firebase Storage
        UploadTask uploadTask = storageReference.putFile(uri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Once the upload is successful, get the download URL
            storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                // Save the download URL to Firestore
                saveProfilePictureUrlToFirestore(downloadUrl.toString());
            });
        }).addOnFailureListener(e -> {
            // Handle any errors during the upload
            showSnackbar(R.string.update_failed);
        });
    }

    private void saveProfilePictureUrlToFirestore(String downloadUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar(R.string.user_not_authenticated);
            return;
        }

        // Get a reference to the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        // UpdLate the user's profile with the new profile picture UR
        userRef.update("profilePictureUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated Firestore
                    showSnackbar(R.string.profile_photo_updated);
                    loadProfilePicture(); // Reload the profile picture after update
                })
                .addOnFailureListener(e -> {
                    // Handle any errors while updating Firestore
                    showSnackbar(R.string.update_failed);
                });
    }

    private void uploadProfilePicture(Uri imageUri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showSnackbar(R.string.user_not_found);
            return;
        }

        // Firebase Storage reference where the image will be stored
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_photos/" + currentUser.getUid() + ".jpg");

        // Upload the image to Firebase Storage
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // On success, get the download URL for the uploaded image
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Get the download URL and save it to Firestore
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

    private void handleGoogleSignIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            // User is not signed in, show the Google Sign-In dialog
            DialogUtil.showGoogleSignInDialog(requireContext(), new DialogUtil.DialogCallback() {
                @Override
                public void onConfirm() {
                    // Proceed with Google Sign-In
                    startGoogleSignIn();
                }

                @Override
                public void onCancel() {
                    // Handle cancel action
                    showSnackbar(R.string.no);
                }
            });
        } else {
            // Handle signed-in user
            nameTextView.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(requireContext()).load(user.getPhotoUrl()).into(profilePicture);
            }
        }
    }
    private void startGoogleSignIn() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Make sure to use the correct Web Client ID
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);  // RC_SIGN_IN is a constant to handle result
    }


    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void setupPasswordResetButton() {
        managePassword.setOnClickListener(v -> {
            // Add your password reset handling logic here
        });
    }

    private void manageEmail() {
        manageEmail.setOnClickListener(v -> {
            // Handle email management
        });
    }
}
