package ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;

public class PrivatePolicyFragment extends Fragment {

    private FirebaseFirestore db;
    private final Map<String, TextView> contentTextViews = new HashMap<>();

    public PrivatePolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirestoreSingleton.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_policy, container, false);
        initializeContentTextViews(view);
        fetchPrivacyPolicy();
        return view;
    }

    /**
     * Initialize TextView mappings with keys corresponding to Firestore fields.
     */
    private void initializeContentTextViews(View view) {
        contentTextViews.put("data_we_collect", view.findViewById(R.id.tvDataWeCollectContent));
        contentTextViews.put("how_we_use_your_data", view.findViewById(R.id.tvHowWeUseYourDataContent));
        contentTextViews.put("data_sharing", view.findViewById(R.id.tvDataSharingContent));
        contentTextViews.put("security", view.findViewById(R.id.tvSecurityContent));
        contentTextViews.put("cookies_and_tracking", view.findViewById(R.id.tvCookiesAndTrackingContent));
        contentTextViews.put("user_rights", view.findViewById(R.id.tvUserRightsContent));
        contentTextViews.put("location_data", view.findViewById(R.id.tvLocationDataContent));
        contentTextViews.put("childrens_privacy", view.findViewById(R.id.tvChildrensPrivacyContent));
        contentTextViews.put("changes_to_privacy_policy", view.findViewById(R.id.tvChangesToPrivacyPolicyContent));
        contentTextViews.put("contact_us", view.findViewById(R.id.tvContactUsContent));
    }

    /**
     * Fetches the Privacy Policy content from Firestore and updates the UI.
     */
    private void fetchPrivacyPolicy() {
        db.collection("legal").document("privacy_policy")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateContentTextViews(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    // Log error or notify the user
                });
    }

    /**
     * Updates the TextViews with the data retrieved from Firestore.
     */
    private void updateContentTextViews(@NonNull com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        for (Map.Entry<String, TextView> entry : contentTextViews.entrySet()) {
            String key = entry.getKey();
            TextView textView = entry.getValue();
            String content = documentSnapshot.getString(key);
            textView.setText(content != null ? content : getString(R.string.default_content));
        }
    }
}
