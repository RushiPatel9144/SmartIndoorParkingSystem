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
    private ProgressBar progressBar;
    private List<Fragment> fragmentList;

    int totalPages = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize the ProgressBar
        progressBar = findViewById(R.id.onBoard_progressBar);

        // Initialize ViewPager2
        viewPager = findViewById(R.id.viewPager);

        // Set up the fragment list
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

        // Set up ViewPager Adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Add page change listener to update progress
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                // Calculate the progress based on the current position and the offset during swipe
                int progress = (int) (((float) position + positionOffset) / (totalPages - 1) * 100);

                // Update the progress bar during the swipe
                progressBar.setProgress(progress);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Ensure the progress bar reaches 100% when the last page is selected
                if (position == totalPages - 1) {
                    progressBar.setProgress(100);
                }
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
