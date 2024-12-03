package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.menu.PromotionFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class Home extends BaseNetworkFragment {

    private TextView tvGreeting, tvAdditionalMessages, tvBrowseNearby, tvFindBestParking, tvPromoHeader, tvPromoCode;
    private ImageView imgTop, imgBrowseNearby, imgPromotions;
    private ScrollView scrollView;
    private Button btnViewMap, btnViewPromotions;
    private FirebaseFirestore db;
    private String userName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        fetchAndDisplayUserName();
        setClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_header);
        tvAdditionalMessages = view.findViewById(R.id.tv_additional_messages);
        imgTop = view.findViewById(R.id.img_top);
        tvBrowseNearby = view.findViewById(R.id.tv_browse_nearby);
        imgBrowseNearby = view.findViewById(R.id.img_browse_nearby);
        tvFindBestParking = view.findViewById(R.id.tv_find_best_parking);
        btnViewMap = view.findViewById(R.id.btn_view_map);
        tvPromoHeader = view.findViewById(R.id.tv_promo_header);
        imgPromotions = view.findViewById(R.id.img_promotions);
        tvPromoCode = view.findViewById(R.id.tv_promo_code);
        btnViewPromotions = view.findViewById(R.id.btn_view_promotions);
    }

    private void fetchAndDisplayUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            Log.d("Home", "Fetching user data for UID: " + uid);

            db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userName = document.getString("firstName");
                        tvGreeting.setText(getGreetingMessageWithUserName());
                        tvAdditionalMessages.setText("Welcome to the Park it\nPark Smart, Live Easy!");
                    } else {
                        Log.d("Home", "No such document");
                    }
                } else {
                    Log.d("Home", "get failed with ", task.getException());
                }
            });
        } else {
            Log.d("Home", "User not authenticated");
        }
    }

    private String getGreetingMessage() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 5 && currentHour < 12) {
            return "Good Morning";
        } else if (currentHour >= 12 && currentHour < 18) {
            return "Good Afternoon";
        } else if (currentHour >= 18 && currentHour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    private String getGreetingMessageWithUserName() {
        return getGreetingMessage() + ", " + userName + "!";
    }

    private void setClickListeners() {
        btnViewMap.setOnClickListener(v -> openParkFragment());
        btnViewPromotions.setOnClickListener(v -> openPromotionFragment());
    }

    private void openParkFragment() {
        loadFragments(new Park(), "park_fragment");
    }

    private void openPromotionFragment() {
        loadFragments(new PromotionFragment(), "promotion_fragment");
    }

    private void loadFragments(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.flFragment, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

