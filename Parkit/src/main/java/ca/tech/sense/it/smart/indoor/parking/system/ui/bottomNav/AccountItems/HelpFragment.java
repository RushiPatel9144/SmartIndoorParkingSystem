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
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.logic.HelpFragmentLogic;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;

public class HelpFragment extends BaseNetworkFragment {

    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etComment;
    private Button btnSubmitHelp;
    private ProgressBar progressBar;
    private HelpFragmentLogic helpFragmentLogic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflateFragmentLayout(inflater, container);

        initializeUIElements(view);

        initializeLogic();

        autofillUserData();

        setupButtonClickListener();

        return view;
    }

    /**
     * Inflates the layout for this fragment.
     */
    private View inflateFragmentLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    /**
     * Initializes the UI elements in the fragment.
     */
    private void initializeUIElements(View view) {
        etName = view.findViewById(R.id.feedback_name);
        etPhone = view.findViewById(R.id.feedback_phone);
        etEmail = view.findViewById(R.id.feedback_email);
        etComment = view.findViewById(R.id.feedback_comment);
        btnSubmitHelp = view.findViewById(R.id.submit_feedback_button);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    /**
     * Initializes the HelpFragmentLogic class.
     */
    private void initializeLogic() {
        helpFragmentLogic = new HelpFragmentLogic(
                requireActivity(),
                etName,
                etPhone,
                etEmail,
                etComment,
                btnSubmitHelp,
                progressBar
        );
    }

    /**
     * Fetches user data to autofill fields.
     */
    private void autofillUserData() {
        helpFragmentLogic.fetchUserData();
    }

    /**
     * Sets up the button click listener for the help submission.
     */
    private void setupButtonClickListener() {
        btnSubmitHelp.setOnClickListener(v -> helpFragmentLogic.submitHelpRequest());
    }
}
