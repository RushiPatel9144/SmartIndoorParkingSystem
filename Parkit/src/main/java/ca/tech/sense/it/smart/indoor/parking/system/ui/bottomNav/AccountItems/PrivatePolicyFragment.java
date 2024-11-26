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
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;

public class PrivatePolicyFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView tvDataWeCollectContent;
    private TextView tvHowWeUseYourDataContent;
    private TextView tvDataSharingContent;
    private TextView tvSecurityContent;
    private TextView tvCookiesAndTrackingContent;
    private TextView tvUserRightsContent;
    private TextView tvLocationDataContent;
    private TextView tvChildrensPrivacyContent;
    private TextView tvChangesToPrivacyPolicyContent;
    private TextView tvContactUsContent;

    public PrivatePolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_policy, container, false);

        // Initialize TextViews
        tvDataWeCollectContent = view.findViewById(R.id.tvDataWeCollectContent);
        tvHowWeUseYourDataContent = view.findViewById(R.id.tvHowWeUseYourDataContent);
        tvDataSharingContent = view.findViewById(R.id.tvDataSharingContent);
        tvSecurityContent = view.findViewById(R.id.tvSecurityContent);
        tvCookiesAndTrackingContent = view.findViewById(R.id.tvCookiesAndTrackingContent);
        tvUserRightsContent = view.findViewById(R.id.tvUserRightsContent);
        tvLocationDataContent = view.findViewById(R.id.tvLocationDataContent);
        tvChildrensPrivacyContent = view.findViewById(R.id.tvChildrensPrivacyContent);
        tvChangesToPrivacyPolicyContent = view.findViewById(R.id.tvChangesToPrivacyPolicyContent);
        tvContactUsContent = view.findViewById(R.id.tvContactUsContent);

        db = FirestoreSingleton.getInstance();
        fetchPrivacyPolicy();
        return view;
    }

    private void fetchPrivacyPolicy() {
        db.collection("legal").document("privacy_policy")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            JSONObject jsonContent = new JSONObject();
                            if (documentSnapshot.contains("data_we_collect")) {
                                jsonContent.put("data_we_collect", documentSnapshot.getString("data_we_collect"));
                            }
                            if (documentSnapshot.contains("how_we_use_your_data")) {
                                jsonContent.put("how_we_use_your_data", documentSnapshot.getString("how_we_use_your_data"));
                            }
                            if (documentSnapshot.contains("data_sharing")) {
                                jsonContent.put("data_sharing", documentSnapshot.getString("data_sharing"));
                            }
                            if (documentSnapshot.contains("security")) {
                                jsonContent.put("security", documentSnapshot.getString("security"));
                            }
                            if (documentSnapshot.contains("cookies_and_tracking")) {
                                jsonContent.put("cookies_and_tracking", documentSnapshot.getString("cookies_and_tracking"));
                            }
                            if (documentSnapshot.contains("user_rights")) {
                                jsonContent.put("user_rights", documentSnapshot.getString("user_rights"));
                            }
                            if (documentSnapshot.contains("location_data")) {
                                jsonContent.put("location_data", documentSnapshot.getString("location_data"));
                            }                            if (documentSnapshot.contains("childrens_privacy")) {
                                jsonContent.put("childrens_privacy", documentSnapshot.getString("childrens_privacy"));
                            }
                            if (documentSnapshot.contains("changes_to_privacy_policy")) {
                                jsonContent.put("changes_to_privacy_policy", documentSnapshot.getString("changes_to_privacy_policy"));
                            }
                            if (documentSnapshot.contains("contact_us")) {
                                jsonContent.put("contact_us", documentSnapshot.getString("contact_us"));
                            }

                            tvDataWeCollectContent.setText(jsonContent.optString("data_we_collect", "N/A"));
                            tvHowWeUseYourDataContent.setText(jsonContent.optString("how_we_use_your_data", "N/A"));
                            tvDataSharingContent.setText(jsonContent.optString("data_sharing", "N/A"));
                            tvSecurityContent.setText(jsonContent.optString("security", "N/A"));
                            tvCookiesAndTrackingContent.setText(jsonContent.optString("cookies_and_tracking", "N/A"));
                            tvUserRightsContent.setText(jsonContent.optString("user_rights", "N/A"));
                            tvLocationDataContent.setText(jsonContent.optString("location_data", "N/A"));
                            tvChildrensPrivacyContent.setText(jsonContent.optString("childrens_privacy", "N/A"));
                            tvChangesToPrivacyPolicyContent.setText(jsonContent.optString("changes_to_privacy_policy", "N/A"));
                            tvContactUsContent.setText(jsonContent.optString("contact_us", "N/A"));
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
