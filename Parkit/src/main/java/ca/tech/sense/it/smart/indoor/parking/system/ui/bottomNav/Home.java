/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */

package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
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

public class Home extends BaseNetworkFragment {

    private TextView tvHeader, tvBrowseNearby, tvFindBestParking, tvPromoHeader, tvPromoCode;
    private ImageView imgTop, imgBrowseNearby, imgPromotions;
    private ScrollView scrollView;
    private Button btnViewMap, btnViewPromotions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Views
        initializeViews(view);

        // Set button click listeners
        setClickListeners();

        return view;
    }

    /**
     * Initializes all views in the fragment.
     * Keeping the initialization separate improves code readability.
     */
    private void initializeViews(View view) {
        tvHeader = view.findViewById(R.id.tv_header);
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

    /**
     * Sets click listeners for buttons.
     * Extracting this logic makes the onCreateView method cleaner.
     */
    private void setClickListeners() {
        btnViewMap.setOnClickListener(v -> openParkFragment());
        btnViewPromotions.setOnClickListener(v -> openPromotionFragment());
    }

    /**
     * Replaces the current fragment with the ParkFragment.
     * This encapsulates the fragment transaction logic and avoids duplication.
     */
    private void openParkFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, new Park());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Replaces the current fragment with the PromotionFragment.
     * This encapsulates the fragment transaction logic and avoids duplication.
     */
    private void openPromotionFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, new PromotionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
