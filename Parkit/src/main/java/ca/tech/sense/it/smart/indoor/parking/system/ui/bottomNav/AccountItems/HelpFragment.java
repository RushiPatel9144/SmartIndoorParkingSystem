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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class HelpFragment extends Fragment {

    private TextView tvHelpTopic, tvHelpDescription;
    private EditText etHelpTopic, etHelpDescription;
    private Button btnSubmitHelp;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                    // Create a new document in 'help' collection
                    Map<String, Object> help = new HashMap<>();
                    help.put("topic", helpTopic);
                    help.put("description", helpDescription);

                    db.collection("help").add(help)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Clear the fields
                                    etHelpTopic.setText("");
                                    etHelpDescription.setText("");
                                    Toast.makeText(getActivity(), "Help request submitted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error adding document", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        return view;
    }
}