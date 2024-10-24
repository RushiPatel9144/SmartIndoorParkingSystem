package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
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

    // Request code for image picking
    private static final int PICK_IMAGE_REQUEST = 1;

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
        View view = inflater.inflate(R.layout.fragment_manage_account, container, false);
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
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            }
        });
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                updateProfilePicture(selectedImageUri);
            }
        }
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
            profilePictureButton.setImageURI(uri);
        } else {
            profilePictureButton.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void showSnackbar(int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                showSnackbar(R.string.permission_denied_to_read_external_storage);
            }
        }
    }
}
