/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.logic.HelpFragmentLogic;

public class HelpFragment extends Fragment {

    private EditText etName, etPhone, etEmail, etComment;
    private Button btnSubmitHelp;
    private ProgressBar progressBar;
    private HelpFragmentLogic helpFragmentLogic;

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

        // Initialize logic class
        helpFragmentLogic = new HelpFragmentLogic(getActivity(), etName, etPhone, etEmail, etComment, btnSubmitHelp, progressBar);

        // Fetch user data from Firestore and autofill fields
        helpFragmentLogic.fetchUserData();

        // Set up button click listener
        btnSubmitHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpFragmentLogic.submitHelpRequest();
            }
        });

        return view;
    }
}

