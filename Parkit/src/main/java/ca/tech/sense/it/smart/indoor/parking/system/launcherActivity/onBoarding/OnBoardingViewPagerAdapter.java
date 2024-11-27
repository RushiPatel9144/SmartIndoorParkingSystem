package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.onBoarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * Custom adapter for managing fragments in ViewPager2 for onboarding screens.
 */
public class OnBoardingViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList;

    /**
     * Constructor for OnBoardingViewPagerAdapter.
     *
     * @param fragmentActivity The hosting FragmentActivity.
     * @param fragmentList     The list of fragments to display in the ViewPager.
     */
    public OnBoardingViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, @NonNull List<Fragment> fragmentList) {
        super(fragmentActivity);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position); // Return the fragment for the given position
    }

    @Override
    public int getItemCount() {
        return fragmentList.size(); // Return the total number of fragments
    }


}
