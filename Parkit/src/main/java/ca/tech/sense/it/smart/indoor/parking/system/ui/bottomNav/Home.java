/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */

package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class Home extends Fragment {

    private TextView tvHeader;
    private ImageView imgTop;
    private ScrollView scrollView;
    private TextView tvBrowseNearby;
    private ImageView imgBrowseNearby;
    private TextView tvFindBestParking;
    private Button btnViewMap;
    private TextView tvPromoHeader;
    private ImageView imgPromotions;
    private TextView tvPromoCode;
    private Button btnViewPromotions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize your views here
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

        // Add any additional logic or listeners here if needed
        // For example:
        // btnViewMap.setOnClickListener(v -> navigateToParkFragment());
        // btnViewPromotions.setOnClickListener(v -> navigateToPromotionFragment());

        return view;
    }
}
