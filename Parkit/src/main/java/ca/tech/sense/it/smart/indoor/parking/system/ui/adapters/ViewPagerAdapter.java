package ca.tech.sense.it.smart.indoor.parking.system.ui.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activitytabs.ActiveBookingFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activitytabs.HistoryBookingFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.activitytabs.UpcomingBookingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ActiveBookingFragment();
            case 1:
                return new UpcomingBookingFragment();
            case 2:
                return new HistoryBookingFragment();
            default:
                return new ActiveBookingFragment(); // Fallback
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Three tabs
    }
}
