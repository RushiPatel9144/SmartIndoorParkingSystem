package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<Fragment> fragmentList;

    int totalPages = 3;
    int progressStep = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);

        fragmentList = new ArrayList<>();
        fragmentList.add(OnboardingFragment.newInstance(getString(R.string.onboard_title1),
                getString(R.string.onboard_text1),
                R.drawable.onboardone, false));

        fragmentList.add(OnboardingFragment.newInstance(getString(R.string.onboard_title2),
                getString(R.string.onboard_text2),
                R.drawable.onboardtwo, false));

        fragmentList.add(OnboardingFragment.newInstance(getString(R.string.onboard_title3),
                getString(R.string.onboard_text3),
                R.drawable.onboardthree, true));

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Add page change listener to update progress
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Calculate progress (e.g., 33% for first page, 66% for second, 100% for last)
                int progress = progressStep * (position + 1);

                // Update the progress bar in the current fragment
                OnboardingFragment currentFragment = (OnboardingFragment) fragmentList.get(position);
                currentFragment.updateProgress(progress);
            }
        });
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}
