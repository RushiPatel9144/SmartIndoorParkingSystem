/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.ViewPagerAdapter;

public class Activity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

        // Set up the ViewPager2 with the adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        // Set up the TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Active");
                    break;
                case 1:
                    tab.setText("Upcoming");
                    break;
                case 2:
                    tab.setText("History");
                    break;
            }
        }).attach();



        // Synchronize the TabLayout with ViewPager2
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        return view;
    }
}
