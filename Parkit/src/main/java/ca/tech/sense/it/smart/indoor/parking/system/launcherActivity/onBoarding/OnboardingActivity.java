package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.onBoarding;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private List<Fragment> fragmentList;
    private static final int TOTAL_PAGES = 3; // Number of onboarding screens

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initializeViews();         // Initialize UI components
        setupFragmentList();       // Prepare fragments for onboarding screens
        setupViewPager();          // Set up ViewPager2 with adapter
        setupPageChangeListener(); // Attach listener to handle progress bar updates
    }

    /**
     * Initializes the UI components used in the activity.
     */
    private void initializeViews() {
        progressBar = findViewById(R.id.onBoard_progressBar);
        viewPager = findViewById(R.id.viewPager);
    }

    /**
     * Sets up the list of fragments for the onboarding flow.
     */
    private void setupFragmentList() {
        fragmentList = new ArrayList<>();

        fragmentList.add(createOnboardingFragment(
                R.string.onboard_title1,
                R.string.onboard_text1,
                R.drawable.onboardone,
                false
        ));

        fragmentList.add(createOnboardingFragment(
                R.string.onboard_title2,
                R.string.onboard_text2,
                R.drawable.onboardtwo,
                false
        ));

        fragmentList.add(createOnboardingFragment(
                R.string.onboard_title3,
                R.string.onboard_text3,
                R.drawable.onboardthree,
                true
        ));
    }

    /**
     * Creates a new instance of OnboardingFragment with specified parameters.
     */
    private OnboardingFragment createOnboardingFragment(int titleRes, int textRes, int imageRes, boolean isLastPage) {
        return OnboardingFragment.newInstance(
                getString(titleRes),
                getString(textRes),
                imageRes,
                isLastPage
        );
    }

    /**
     * Configures the ViewPager2 with an adapter and attaches the fragment list.
     */
    private void setupViewPager() {
        if (fragmentList == null || fragmentList.isEmpty()) {
            throw new IllegalStateException("Fragment list is empty. Please add fragments before setting up the ViewPager.");
        }
        OnBoardingViewPagerAdapter adapter = new OnBoardingViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(adapter);
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public List<Fragment> getFragmentList() {
        return fragmentList;
    }


    /**
     * Sets up a listener for ViewPager2 to update the progress bar as pages are swiped.
     */
    private void setupPageChangeListener() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                // Set progress to 100% on the last page
                if (position == 0 ) {
                    progressBar.setProgress(33);
                } else if (position == 1) {
                    progressBar.setProgress(66);
                } else if (position == 2) {
                    progressBar.setProgress(100);
                }
            }
        });
    }


}
