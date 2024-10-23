package ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ManageAccountFragment extends Fragment {

    // Constants for shared preferences
    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";

    // Views
    private ImageButton profilePictureButton;
    private TextView nameTextView;
    private TextView contactDetailsTextView;

    // ActivityResultLaunchers for permissions and image picking
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                    showSnackbar(R.string.permission_granted_to_read_external_storage);
                } else {
                    showSnackbar(R.string.permission_denied_to_read_external_storage);
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        updateProfilePicture(selectedImageUri);
                    }
                }
            });

    public ManageAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        bindViews(view);
        loadProfilePictureUri();
        setupProfilePictureButton();
        return view;
    }

    private void bindViews(View view) {
        profilePictureButton = view.findViewById(R.id.imageButton);
        nameTextView = view.findViewById(R.id.name);
        contactDetailsTextView = view.findViewById(R.id.contact_details);

        // Set default values or fetch actual data from storage/database
        profilePictureButton.setImageResource(R.mipmap.ic_launcher);
        nameTextView.setText(R.string.the_tech_sense); // Replace with actual name
        contactDetailsTextView.setText(R.string.thetechsense123_gmail_com); // Replace with actual contact
    }

    private void setupProfilePictureButton() {
        profilePictureButton.setOnClickListener(v -> {
            if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getString(R.string.image));
        pickImageLauncher.launch(intent);
    }

    private void updateProfilePicture(Uri uri) {
        profilePictureButton.setImageURI(uri);
        saveProfilePictureUri(uri);
    }

    private void saveProfilePictureUri(Uri uri) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_PROFILE_PICTURE_URI, uri.toString()).apply();
    }

    private void loadProfilePictureUri() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null);
        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri)) {
                profilePictureButton.setImageURI(uri);
            } catch (Exception e) {
                profilePictureButton.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            profilePictureButton.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void showSnackbar(int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }
}
