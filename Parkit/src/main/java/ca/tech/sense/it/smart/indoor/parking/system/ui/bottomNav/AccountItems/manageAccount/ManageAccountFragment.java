package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.utility.AuthUtils;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ImageCropActivity;

public class ManageAccountFragment extends Fragment {

    private static final String COLLECTION_USER = "users";
    private static final String COLLECTION_OWNER = "owners";
    private String collection = COLLECTION_USER;
    private ImageView profilePicture;
    private TextView nameTextView;
    private TextView contactDetailsTextView;
    private TextView phoneNumberTextView;
    private LinearLayout manageProfilePicture;
    private LinearLayout managePassword;
    private LinearLayout manageEmail;
    private LinearLayout managePhoneNumber;
    private LinearLayout manageName;
    private View rootView;
    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseUser currentUser;
    private ProfileEditManager profileEditManager;
    private ProfilePictureManager profilePictureManager;
    private FirebaseAuth mAuth;

    // Executor service to schedule tasks
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    // Activity result launchers for image selection and cropping
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
                        profilePictureManager.saveProfilePicture(croppedImageUri, profilePicture);
                        profilePictureManager.uploadProfilePicture(croppedImageUri); // Upload new profile picture to Firebase
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_account, container, false);

        // Initialize UI components and Firebase instances
        bindViews(rootView);
        mAuth = FirebaseAuthSingleton.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirestoreSingleton.getInstance();

        // Check user type (Owner/User) and initialize manager classes
        checkUserType();
        initializeManagers();

        // Fetch user details and set profile picture
        scheduleUserDetailsFetch();
        swipeRefreshLayout.setOnRefreshListener(this::scheduleUserDetailsFetch);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup action buttons
        setupProfilePictureButton();
        setupEditButtons();
    }

    // Bind UI components
    private void bindViews(View view) {
        profilePicture = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberManage);
        manageProfilePicture = view.findViewById(R.id.manageProfilePic);
        managePassword = view.findViewById(R.id.managePassword);
        manageEmail = view.findViewById(R.id.manageEmail);
        managePhoneNumber = view.findViewById(R.id.managePhoneNumber);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        manageName = view.findViewById(R.id.manageName);
    }

    // Setup profile picture update button
    private void setupProfilePictureButton() {
        manageProfilePicture.setOnClickListener(v -> {
            if (isPermissionGranted()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });
    }

    // Check if storage permission is granted
    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    // Open image gallery to select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getString(R.string.image1));
        imagePickerLauncher.launch(intent);
    }

    // Crop the selected image
    private void cropImage(Uri imageUri) {
        Intent intent = new Intent(getActivity(), ImageCropActivity.class);
        intent.putExtra(getString(R.string.imageuri), imageUri);
        imageCropLauncher.launch(intent);
    }

    // Load the cropped image into the profile picture view
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

    // Initialize ProfileEditManager and ProfilePictureManager
    private void initializeManagers() {
        profileEditManager = new ProfileEditManager(this, db, collection, currentUser.getUid());
        profilePictureManager = new ProfilePictureManager(this, db, collection, mAuth);
    }

    // Fetch user details from Firestore after a brief delay
    private void scheduleUserDetailsFetch() {
        swipeRefreshLayout.setRefreshing(true);
        scheduledExecutorService.schedule(() -> {
            profileEditManager.fetchUserDetailsFromFirestore(mAuth, contactDetailsTextView, nameTextView, phoneNumberTextView);
            profilePictureManager.loadProfilePicture(profilePicture);
            requireActivity().runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
        }, 2, TimeUnit.SECONDS);
    }

    // Setup buttons for managing profile information (password, email, phone number)
    private void setupEditButtons() {
        managePassword.setOnClickListener(v -> AuthUtils.showResetPasswordDialog(requireContext(), FirebaseAuth.getInstance()));
        manageEmail.setOnClickListener(view -> profileEditManager.manageEmail());
        managePhoneNumber.setOnClickListener(view -> profileEditManager.managePhoneNumber());
        manageName.setOnClickListener(view -> profileEditManager.manageName());
    }

    // Check the user type (Owner/User) and update collection reference accordingly
    private void checkUserType() {
        SessionManager sessionManager = SessionManager.getInstance(requireContext());

        if (sessionManager == null || sessionManager.getUserType() == null) {
            // Fetch session data asynchronously if session is not initialized
            Objects.requireNonNull(sessionManager).fetchSessionData((userType, ownerType) -> {
                if (ownerType != null) {
                    collection = COLLECTION_OWNER;
                } else if (userType != null) {
                    collection = COLLECTION_USER;
                }
            });
        } else {
            if (Objects.equals(sessionManager.getUserType(), "owner")) {
                collection = COLLECTION_OWNER;
            } else {
                collection = COLLECTION_USER;
            }
        }
    }


    // Show a Snackbar message
    private void showSnackbar(int messageResId) {
        if (isAdded() && getView() != null) {
            Snackbar.make(rootView, messageResId, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }
}
