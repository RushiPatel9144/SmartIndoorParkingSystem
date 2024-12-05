package ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.manageAccount;

import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class ProfileEditManager {

    private final FirebaseFirestore db;
    private final String collection;
    private final String userId;
    private final Fragment fragment;


    public ProfileEditManager(Fragment fragment, FirebaseFirestore db, String collection, String userId) {
        this.fragment = fragment;
        this.db = db;
        this.collection = collection;
        this.userId = userId;
    }

    public void manageName() {
        if (fragment.isAdded()) {
            DialogUtil.showInputDialog(
                    fragment.requireContext(),
                    fragment.getString(R.string.set_display_name),
                    fragment.getString(R.string.enter_display_name_at_least_3_characters), // Default value (can be empty)
                    new DialogUtil.InputDialogCallback() {
                        @Override
                        public void onConfirm(String inputText) {
                            if (inputText != null && inputText.trim().length() >= 3) {
                                inputText = inputText.trim();
                                String[] nameParts = inputText.split(" ", 2); // Split by first space only

                                String firstName = nameParts[0]; // First name is the first part
                                String lastName = (nameParts.length > 1) ? nameParts[1] : "";

                                // Update both first and last name in Firestore
                                updateNameInFirestore(firstName, lastName);
                            }
                        }
                        @Override
                        public void onCancel() {
                            // Optionally, handle dialog cancellation
                        }
                    }
            );
        }
    }

    private void updateNameInFirestore(String firstName, String lastName) {
        DocumentReference userRef = FirestoreSingleton.getInstance()
                .collection(collection)
                .document(Objects.requireNonNull(userId));

        userRef.update("firstName", firstName, "lastName", lastName)
                .addOnSuccessListener(aVoid -> showSnackbar(R.string.profile_name_updated))
                .addOnFailureListener(e -> showSnackbar(R.string.update_failed));
    }

    public void managePhoneNumber() {
        if (fragment.isAdded()){
        DialogUtil.showInputDialog(
                fragment.requireContext(),
                fragment.getString(R.string.set_phone_number),
                "+1XXXXXXXXXX",
                new DialogUtil.InputDialogCallback() {
                    @Override
                    public void onConfirm(String inputText) {
                        if (inputText != null && inputText.trim().length() >= 12) {
                            updatePhoneNumberInFirestore(inputText);
                        } else {
                            showSnackbar(R.string.invalid_phone_number);
                        }
                    }
                    @Override
                    public void onCancel() {
                        // Optionally, handle dialog cancellation
                    }
                }
        );
    }}

    private void updatePhoneNumberInFirestore(String phoneNumber) {
        if (userId != null) {
            DocumentReference userRef = db.collection(collection).document(userId);
            userRef.update("phone", phoneNumber)
                    .addOnSuccessListener(aVoid -> showSnackbar(R.string.phone_number_updated))
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", fragment.getString(R.string.failed_to_update_phone_number), e);
                        showSnackbar(R.string.phone_number_update_failed);
                    });
        } else {
            showSnackbar(R.string.user_not_authenticated);
        }
    }

    public void manageEmail() {
        if (fragment.isAdded()){
        DialogUtil.showMessageDialog(
                fragment.requireContext(),
                fragment.getString(R.string.email_update_unavailable),
                fragment.getString(R.string.changing_your_email_address_is_currently_not_permitted_please_reach_out_to_our_support_team_for_further_assistance),
                fragment.getString(R.string.help),
                new DialogUtil.DialogCallback() {
                    @Override
                    public void onConfirm() {
                        fragment.getParentFragmentManager().beginTransaction()
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
    }
    }

    public void fetchUserDetailsFromFirestore(FirebaseAuth mAuth, TextView contactDetailsTextView, TextView nameTextView, TextView phoneNumberTextView) {
        if (mAuth == null || mAuth.getUid() == null || mAuth.getCurrentUser() == null) {
            showSnackbar(R.string.auth_error_message);
            return;
        }

        setContactDetails(contactDetailsTextView, mAuth);
        fetchDocumentData(mAuth.getUid(), nameTextView, phoneNumberTextView);
    }

    private void setContactDetails(TextView contactDetailsTextView, FirebaseAuth mAuth) {
        String email = Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail(), "Email must not be null");
        contactDetailsTextView.setText(email);
    }

    private void fetchDocumentData(String uid, TextView nameTextView, TextView phoneNumberTextView) {
        DocumentReference docRef = db.collection(collection).document(uid);

        docRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                showSnackbar(R.string.fetch_data_failed);
                return;
            }

            DocumentSnapshot document = task.getResult();
            if (document == null || !document.exists()) {
                showSnackbar(R.string.user_data_not_found);
                return;
            }

            updateUserDetails(document, nameTextView, phoneNumberTextView);
        });
    }

    private void updateUserDetails(DocumentSnapshot document, TextView nameTextView, TextView phoneNumberTextView) {
        String firstName = document.getString("firstName");
        String lastName = document.getString("lastName");
        String phoneNumber = document.getString("phone");

        updateNameTextView(nameTextView, firstName, lastName);
        updatePhoneNumberTextView(phoneNumberTextView, phoneNumber);
    }

    private void updateNameTextView(TextView nameTextView, String firstName, String lastName) {
        if (firstName != null) {
            nameTextView.setText(firstName);
        }
        if (lastName != null) {
            nameTextView.append(" " + lastName);
        }
    }

    private void updatePhoneNumberTextView(TextView phoneNumberTextView, String phoneNumber) {
        if (phoneNumber != null) {
            phoneNumberTextView.setText(phoneNumber);
        } else {
            phoneNumberTextView.setText(R.string.add_phone_number);
        }
    }


    private void showSnackbar(int messageResId) {
        if (fragment.isAdded() && fragment.getView() != null) {
            Snackbar.make(fragment.requireView(), messageResId, -1).show();
        }
    }
}
