package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONException;
import org.json.JSONObject;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class TermsOfUseFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView tvAcceptanceOfTermsContent;
    private TextView tvPurposeOfTheAppContent;
    private TextView tvUserResponsibilitiesContent;
    private TextView tvBookingAndPaymentContent;
    private TextView tvParkingRulesContent;
    private TextView tvAccountManagementContent;
    private TextView tvRestrictionsContent;
    private TextView tvModificationsToTheAppContent;
    private TextView tvLiabilityContent;

    public TermsOfUseFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms_of_use, container, false);

        // Initialize TextViews
        tvAcceptanceOfTermsContent = view.findViewById(R.id.tvAcceptanceOfTermsContent);
        tvPurposeOfTheAppContent = view.findViewById(R.id.tvPurposeOfTheAppContent);
        tvUserResponsibilitiesContent = view.findViewById(R.id.tvUserResponsibilitiesContent);
        tvBookingAndPaymentContent = view.findViewById(R.id.tvBookingAndPaymentContent);
        tvParkingRulesContent = view.findViewById(R.id.tvParkingRulesContent);
        tvAccountManagementContent = view.findViewById(R.id.tvAccountManagementContent);
        tvRestrictionsContent = view.findViewById(R.id.tvRestrictionsContent);
        tvModificationsToTheAppContent = view.findViewById(R.id.tvModificationsToTheAppContent);
        tvLiabilityContent = view.findViewById(R.id.tvLiabilityContent);

        db = FirebaseFirestore.getInstance();

        fetchTermsOfUse();
        return view;
    }

    private void fetchTermsOfUse() {
        db.collection("legal").document("terms_of_use")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            JSONObject jsonContent = new JSONObject();
                            if (documentSnapshot.contains("acceptance_of_terms")) {
                                jsonContent.put("acceptance_of_terms", documentSnapshot.getString("acceptance_of_terms"));
                            }
                            if (documentSnapshot.contains("purpose_of_the_app")) {
                                jsonContent.put("purpose_of_the_app", documentSnapshot.getString("purpose_of_the_app"));
                            }
                            if (documentSnapshot.contains("user_responsibilities")) {
                                jsonContent.put("user_responsibilities", documentSnapshot.getString("user_responsibilities"));
                            }
                            if (documentSnapshot.contains("booking_and_payment")) {
                                jsonContent.put("booking_and_payment", documentSnapshot.getString("booking_and_payment"));
                            }
                            if (documentSnapshot.contains("parking_rules")) {
                                jsonContent.put("parking_rules", documentSnapshot.getString("parking_rules"));
                            }
                            if (documentSnapshot.contains("account_management")) {
                                jsonContent.put("account_management", documentSnapshot.getString("account_management"));
                            }
                            if (documentSnapshot.contains("restrictions")) {
                                jsonContent.put("restrictions", documentSnapshot.getString("restrictions"));
                            }
                            if (documentSnapshot.contains("modifications_to_the_app")) {
                                jsonContent.put("modifications_to_the_app", documentSnapshot.getString("modifications_to_the_app"));
                            }
                            if (documentSnapshot.contains("liability")) {
                                jsonContent.put("liability", documentSnapshot.getString("liability"));
                            }

                            tvAcceptanceOfTermsContent.setText(jsonContent.optString("acceptance_of_terms", "N/A"));
                            tvPurposeOfTheAppContent.setText(jsonContent.optString("purpose_of_the_app", "N/A"));
                            tvUserResponsibilitiesContent.setText(jsonContent.optString("user_responsibilities", "N/A"));
                            tvBookingAndPaymentContent.setText(jsonContent.optString("booking_and_payment", "N/A"));
                            tvParkingRulesContent.setText(jsonContent.optString("parking_rules", "N/A"));
                            tvAccountManagementContent.setText(jsonContent.optString("account_management", "N/A"));
                            tvRestrictionsContent.setText(jsonContent.optString("restrictions", "N/A"));
                            tvModificationsToTheAppContent.setText(jsonContent.optString("modifications_to_the_app", "N/A"));
                            tvLiabilityContent.setText(jsonContent.optString("liability", "N/A"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }
}
