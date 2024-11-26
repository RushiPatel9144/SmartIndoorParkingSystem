package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount;

import static ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount.ManageAccountFireBaseHelper.showSnackbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.canhub.cropper.CropImageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
import ca.tech.sense.it.smart.indoor.parking.system.utility.UserCheckHelper;

public class ManageAccountFragment extends Fragment {

    // Declare UI elements
    private ImageView profilePicture;
    private TextView nameTextView, contactDetailsTextView, phoneNumberTextView;
    private LinearLayout manageProfilePicture, managePassword, manageEmail, managePhoneNumber;
    private FrameLayout progressFrame;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String collection = "users"; // Default collection

    // Executor for scheduling tasks
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    // Activity Result Launchers for image selection and cropping
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
                        ManageAccountHelper.loadCroppedImage(croppedImageUri,requireContext(), profilePicture);
                        ManageAccountFireBaseHelper.saveProfilePicture(croppedImageUri, currentUser, requireView(), db, collection, profilePicture, requireContext());
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (Boolean.TRUE.equals(isGranted)) {
                    ManageAccountHelper.openGallery(imagePickerLauncher);
                } else {
                    showSnackbar(R.string.permission_denied_to_read_external_storage,requireView()
                    );
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_account, container, false);
        bindViews(rootView);
        FirebaseAuth mAuth = FirebaseAuthSingleton.getInstance();
        currentUser = mAuth.getCurrentUser();
        SessionManager sessionManager = new SessionManager(requireContext());

// Fetch session data (if not already fetched)
        sessionManager.fetchSessionData((user, owner) -> {

        });
        db = FirestoreSingleton.getInstance();
        checkUserType();
        scheduleCheckUserType();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupProfilePictureButton();
        setupPasswordResetButton();
        setupManageEmailButton();
        setupManagePhoneNumberButton();
    }

    // === Helper Methods ===

    private void bindViews(View view) {
        profilePicture = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameEdit);
        contactDetailsTextView = view.findViewById(R.id.emailEdit);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberManage);
        manageProfilePicture = view.findViewById(R.id.manageProfilePic);
        managePassword = view.findViewById(R.id.managePassword);
        manageEmail = view.findViewById(R.id.manageEmail);
        managePhoneNumber = view.findViewById(R.id.managePhoneNumber);
        progressFrame = view.findViewById(R.id.progressFrame);
    }

    private void setupProfilePictureButton() {
        manageProfilePicture.setOnClickListener(v -> {
            if (ManageAccountHelper.isPermissionGranted(requireContext())) {
                ManageAccountHelper.openGallery(imagePickerLauncher);
            } else {
                ManageAccountHelper.requestStoragePermission(requestPermissionLauncher);
            }
        });
    }

    private void setupPasswordResetButton() {
        managePassword.setOnClickListener(v -> AuthUtils.showResetPasswordDialog(requireContext(), FirebaseAuth.getInstance()));
    }

    private void setupManageEmailButton() {
        manageEmail.setOnClickListener(v -> DialogUtil.showMessageDialog(
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
                    public void onCancel() { /* Do nothing */ }
                }
        ));
    }

    private void setupManagePhoneNumberButton() {
        managePhoneNumber.setOnClickListener(v -> DialogUtil.showInputDialog(
                requireContext(),
                getString(R.string.set_phone_number),
                "+1XXXXXXXXXX",
                new DialogUtil.InputDialogCallback() {
                    @Override
                    public void onConfirm(String inputText) {
                        if (inputText != null && inputText.trim().length() >= 12) {
                            ManageAccountFireBaseHelper.updatePhoneNumberInFirestore(inputText.trim(), currentUser, db, collection, phoneNumberTextView, requireView());
                        } else {
                            showSnackbar(R.string.invalid_phone_number,requireView());
                        }
                    }
                    @Override
                    public void onCancel() { /* Do nothing */ }
                }
        ));
    }

    // === Firebase and Storage Operations ===
    private void checkUserType() {
        if (currentUser != null) {
            UserCheckHelper userCheckHelper = new UserCheckHelper();
            userCheckHelper.checkUserType(currentUser.getUid(), getContext(), new UserCheckHelper.UserTypeCallback() {
                @Override
                public void onUserTypeDetermined(UserCheckHelper.UserType userType) {
                    collection = (userType == UserCheckHelper.UserType.OWNER) ? "owners" : "users";
                }
                @Override
                public void onError() {
                    Toast.makeText(getContext(), R.string.an_error_occurred, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.no_user_is_logged_in, Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleCheckUserType() {
        scheduledExecutorService.schedule(() -> {
            if (currentUser != null) {
                String uid = currentUser.getUid();
                SessionManager sessionManager = new SessionManager(requireContext());
                sessionManager.fetchSessionData((user, owner) -> {
                            if (user != null) {
                                nameTextView.setText(user.getFirstName() + " " + user.getLastName());
                                contactDetailsTextView.setText(user.getEmail());
                                phoneNumberTextView.setText(user.getPhone());
                            }
                            if (owner != null) {
                                nameTextView.setText(owner.getFirstName() + " " + owner.getLastName());
                                contactDetailsTextView.setText(owner.getEmail());
                                phoneNumberTextView.setText(owner.getPhone());
                            }
                        });
//               ManageAccountFireBaseHelper.fetchUserDetailsFromFirestore(uid, db, collection, nameTextView, phoneNumberTextView, requireView(),sessionManager);
                ManageAccountFireBaseHelper.loadProfilePicture(currentUser, db, collection, profilePicture, requireView(), requireContext());
            }
        }, 1, TimeUnit.SECONDS);
    }
    private void cropImage(Uri uri) {
        Intent cropIntent = new Intent(requireContext(), CropImageActivity.class);  // Adjust as per your crop activity
        cropIntent.putExtra("imageUri", uri.toString());
        imageCropLauncher.launch(cropIntent);
    }
}

