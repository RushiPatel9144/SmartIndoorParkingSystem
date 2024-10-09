package ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer;

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
import android.widget.Toast;
import ca.tech.sense.it.smart.indoor.parking.system.R;

import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

import com.google.android.material.snackbar.Snackbar;

public class AccountFragment extends Fragment {

    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";

    private ImageButton profilePictureButton;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                    Snackbar.make(getView(), (R.string.permission_granted_to_read_external_storage), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(getView(), (R.string.permission_denied_to_read_external_storage), Snackbar.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        profilePictureButton.setImageURI(selectedImageUri);
                        saveProfilePictureUri(selectedImageUri);
                    }
                }
            });

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Bind views
        profilePictureButton = view.findViewById(R.id.imageButton);
        TextView name = view.findViewById(R.id.name);
        TextView contactDetails = view.findViewById(R.id.contact_details);

        // Set data (replace with actual data)
        profilePictureButton.setImageResource(R.mipmap.ic_launcher); // Replace with actual image resource or URL
        name.setText(R.string.the_tech_sense); // Replace with actual name
        contactDetails.setText(R.string.thetechsense123_gmail_com); // Replace with actual contact details

        // Load saved profile picture URI
        loadProfilePictureUri();

        // Set click listener for the ImageButton
        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for runtime permissions
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    // Permission already granted, open gallery
                    openGallery();
                }
            }
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getString(R.string.image));
        pickImageLauncher.launch(intent);
    }

    private void saveProfilePictureUri(Uri uri) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_PICTURE_URI, uri.toString());
        editor.apply();
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
        }
        else {
            profilePictureButton.setImageResource(R.mipmap.ic_launcher);
        }
    }
}