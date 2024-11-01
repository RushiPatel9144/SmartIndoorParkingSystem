package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AuthUtils;

public class ManageAccountFragment extends Fragment {
    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";
    private ImageView profilePicture;
    private TextView nameTextView;
    private TextView contactDetailsTextView;
    private TextView phoneNumberTextView;
    private TextView passwordTextView;
    private LinearLayout ManageProfilePicture;
    private LinearLayout ManageName;
    private LinearLayout ManageContactDetail;
    private LinearLayout ManagePhoneNumber;
    private LinearLayout ManagePassword;
    private Uri imageUri;
    private View rootView; // Store the root view for later access

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    showSnackbar(R.string.permission_denied_to_read_external_storage);
                }
            }
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profilePicture.setImageURI(imageUri);
                    saveProfilePictureUri(imageUri);
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment and save the root view
        rootView = inflater.inflate(R.layout.fragment_manage_account, container, false);
        bindViews(rootView);
        fetchUserInfo();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProfilePicture();
        setupProfilePictureButton();
        setupPasswordResetButton();
    }

    private void bindViews(View view) {
        profilePicture = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        profilePicture = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberManage);
        passwordTextView = view.findViewById(R.id.passwordManage);
        ManageProfilePicture = view.findViewById(R.id.manageProfilePic);
        ManageName=view.findViewById(R.id.manageName);
        ManageContactDetail=view.findViewById(R.id.manageEmail);
        ManagePhoneNumber=view.findViewById(R.id.managePhoneNumber);
        ManagePassword=view.findViewById(R.id.managePassword);

    }

    private void setupProfilePictureButton() {
        ManageProfilePicture.setOnClickListener(v -> {
            if (isPermissionGranted()) {
                openGallery();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });
    }

    private void fetchUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            // Update the UI with the user's email
            if (email != null) {
                contactDetailsTextView.setText(email);
            }

            String uid = user.getUid(); // Get the unique user ID
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(uid);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Fetch first name, last name, and phone number
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String phoneNumber = document.getString("phone");

                        // Update UI with fetched details
                        if (firstName != null) {
                            nameTextView.setText(firstName);
                        }
                        if (lastName != null) {
                            nameTextView.append(" "+lastName);
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

        } else {
            showSnackbar(R.string.user_not_authenticated);
        }
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadProfilePicture() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null);
        if (uriString != null) {
            try {
                Uri uri = Uri.parse(uriString);
                profilePicture.setImageURI(uri);
            } catch (Exception e) {
                profilePicture.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            profilePicture.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void saveProfilePictureUri(Uri uri) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_PROFILE_PICTURE_URI, uri.toString()).apply();
    }

    private void showSnackbar(int messageId) {
        if (rootView != null) {
            Snackbar.make(rootView, messageId, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void setupPasswordResetButton() {
        ManagePassword.setOnClickListener(v -> AuthUtils.showResetPasswordDialog(requireContext(), FirebaseAuth.getInstance()));
    }

}
