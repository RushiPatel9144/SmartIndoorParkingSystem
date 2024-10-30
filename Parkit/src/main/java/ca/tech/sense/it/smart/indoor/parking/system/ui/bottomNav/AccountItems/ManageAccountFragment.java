package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import static ca.tech.sense.it.smart.indoor.parking.system.R.drawable.manage_account;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ManageAccountFragment extends Fragment {
    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";
    private ImageView profilePictureButton;
    private TextView nameTextView;
    private TextView contactDetailsTextView;
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
                    profilePictureButton.setImageURI(imageUri);
                    saveProfilePictureUri(imageUri);
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment and save the root view
        rootView = inflater.inflate(R.layout.fragment_manage_account, container, false);
        bindViews(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProfilePicture();
        setupProfilePictureButton();
    }

    private void bindViews(View view) {
        profilePictureButton = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        nameTextView.setText(R.string.the_tech_sense);
        contactDetailsTextView.setText(R.string.thetechsense123_gmail_com);
    }

    private void setupProfilePictureButton() {
        profilePictureButton.setOnClickListener(v -> {
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
                profilePictureButton.setImageURI(uri);
            } catch (Exception e) {
                profilePictureButton.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            profilePictureButton.setImageResource(R.mipmap.ic_launcher);
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
}
