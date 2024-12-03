package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.COLLECTION_LEGAL;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;

public class TermsOfUseFragment extends Fragment {

    private FirebaseFirestore db;
    private HashMap<String, TextView> termsTextViews;

    // Constants for the term keys
    private static final String ACCEPTANCE_OF_TERMS = "acceptance_of_terms";
    private static final String PURPOSE_OF_THE_APP = "purpose_of_the_app";
    private static final String USER_RESPONSIBILITIES = "user_responsibilities";
    private static final String BOOKING_AND_PAYMENT = "booking_and_payment";
    private static final String PARKING_RULES = "parking_rules";
    private static final String ACCOUNT_MANAGEMENT = "account_management";
    private static final String RESTRICTIONS = "restrictions";
    private static final String MODIFICATIONS_TO_THE_APP = "modifications_to_the_app";
    private static final String LIABILITY = "liability";

    public TermsOfUseFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms_of_use, container, false);

        // Initialize the terms' TextViews in a HashMap
        initializeTermsTextViews(view);

        db = FirestoreSingleton.getInstance();

        fetchTermsOfUse();
        return view;
    }

    private void initializeTermsTextViews(View view) {
        termsTextViews = new HashMap<>();
        termsTextViews.put(ACCEPTANCE_OF_TERMS, view.findViewById(R.id.tvAcceptanceOfTermsContent));
        termsTextViews.put(PURPOSE_OF_THE_APP, view.findViewById(R.id.tvPurposeOfTheAppContent));
        termsTextViews.put(USER_RESPONSIBILITIES, view.findViewById(R.id.tvUserResponsibilitiesContent));
        termsTextViews.put(BOOKING_AND_PAYMENT, view.findViewById(R.id.tvBookingAndPaymentContent));
        termsTextViews.put(PARKING_RULES, view.findViewById(R.id.tvParkingRulesContent));
        termsTextViews.put(ACCOUNT_MANAGEMENT, view.findViewById(R.id.tvAccountManagementContent));
        termsTextViews.put(RESTRICTIONS, view.findViewById(R.id.tvRestrictionsContent));
        termsTextViews.put(MODIFICATIONS_TO_THE_APP, view.findViewById(R.id.tvModificationsToTheAppContent));
        termsTextViews.put(LIABILITY, view.findViewById(R.id.tvLiabilityContent));
    }

    private void fetchTermsOfUse() {
        db.collection(COLLECTION_LEGAL).document("terms_of_use")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateTermsContent(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.default_content), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTermsContent(@NonNull DocumentSnapshot documentSnapshot) {
        for (String term : termsTextViews.keySet()) {
            String content = documentSnapshot.getString(term);
            updateTextView(term, content);
        }
    }

    private void updateTextView(String term, String content) {
        TextView textView = termsTextViews.get(term);
        if (textView != null) {
            textView.setText(content != null ? content : getString(R.string.default_content));
        }
    }

}
