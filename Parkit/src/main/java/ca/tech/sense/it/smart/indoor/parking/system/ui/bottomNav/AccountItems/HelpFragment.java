package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class HelpFragment extends Fragment {

    private TextView tvHelpTopic, tvHelpDescription;
    private EditText etHelpTopic, etHelpDescription;
    private Button btnSubmitHelp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // Initialize UI elements
        tvHelpTopic = view.findViewById(R.id.tvHelpTopic);
        tvHelpDescription = view.findViewById(R.id.tvHelpDescription);
        etHelpTopic = view.findViewById(R.id.etHelpTopic);
        etHelpDescription = view.findViewById(R.id.etHelpDescription);
        btnSubmitHelp = view.findViewById(R.id.btnSubmitHelp);

        // Set up button click listener
        btnSubmitHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String helpTopic = etHelpTopic.getText().toString().trim();
                String helpDescription = etHelpDescription.getText().toString().trim();

                if (helpTopic.isEmpty() || helpDescription.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill in both fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the submit action (e.g., send to a server or save locally)
                    Toast.makeText(getActivity(), "Help request submitted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}