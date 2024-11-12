/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Help;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class HelpFragment extends Fragment {

    private EditText etName, etPhone, etEmail, etComment;
    private Button btnSubmitHelp;
    private ProgressBar progressBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // Initialize UI elements
        etName = view.findViewById(R.id.feedback_name);
        etPhone = view.findViewById(R.id.feedback_phone);
        etEmail = view.findViewById(R.id.feedback_email);
        etComment = view.findViewById(R.id.feedback_comment);
        btnSubmitHelp = view.findViewById(R.id.submit_feedback_button);
        progressBar = view.findViewById(R.id.progress_bar);


        // Fetch user data from Firestore and autofill fields
        String currentUserID = auth.getCurrentUser().getUid();
        db.collection("users").document(currentUserID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            etName.setText(documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"));
                            etPhone.setText(documentSnapshot.getString("phone"));
                            etEmail.setText(documentSnapshot.getString("email"));
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set up button click listener
        btnSubmitHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String comment = etComment.getText().toString().trim();

                if (name.isEmpty()) {
                    etName.setError("Please fill the name");
                    etName.requestFocus();
                } else if (phone.isEmpty()) {
                    etPhone.setError("Please fill the phone number");
                    etPhone.requestFocus();
                } else if (email.isEmpty()) {
                    etEmail.setError("Please fill the email address");
                    etEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError(getString(R.string.please_enter_a_valid_email_address));
                    etEmail.requestFocus();
                } else if (comment.isEmpty()) {
                    etComment.setError("Please describe the issue");
                    etComment.requestFocus();
                }
                else {
                    // Show progress bar and hide submit button
                    progressBar.setVisibility(View.VISIBLE);
                    btnSubmitHelp.setVisibility(View.GONE);

                    // Simulate delay of 5 seconds
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                    // Create a new Help object
                    Help help = new Help(name, phone, email, comment);

                    // Add the Help object to the 'help' collection
                    db.collection("help").add(help)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Clear the fields
                                    etName.setText("");
                                    etPhone.setText("");
                                    etEmail.setText("");
                                    etComment.setText("");

                                    // Show confirmation dialog
                                    String message = getString(R.string.help_request_submitted);
                                    DialogUtil.showConfirmationDialogWithEmail(getActivity(), "Confirmation", message, email, getString(R.string.ok), new DialogUtil.ConfirmDialogCallback() {
                                        @Override
                                        public void onConfirm() {
                                            // Dialog will be dismissed automatically
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Show error message
                                    Toast.makeText(getActivity(), getString(R.string.error_adding_document), Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Hide progress bar and show submit button
                            progressBar.setVisibility(View.GONE);
                            btnSubmitHelp.setVisibility(View.VISIBLE);
                        }
                    }, 5000); // 5 seconds delay
                }
            }
        });

        return view;
    }
}
