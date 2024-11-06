/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activity.ActiveFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activity.HistoryFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activity.UpcomingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                Log.d("ViewPagerAdapter", "Creating ActiveFragment");
                return new ActiveFragment();
            case 1:
                Log.d("ViewPagerAdapter", "Creating UpcomingFragment");
                return new UpcomingFragment();
            case 2:
                Log.d("ViewPagerAdapter", "Creating HistoryFragment");
                return new HistoryFragment();
            default:
                return new ActiveFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Total number of tabs
    }
}
