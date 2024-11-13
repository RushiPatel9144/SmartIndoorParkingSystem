package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.os.Bundle;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);

        fragmentList = new ArrayList<>();
        fragmentList.add(OnboardingFragment.newInstance("Welcome to Parkit!",
                "Whether you're looking for a parking spot or offering one, Parkit makes parking easier for everyone. Let's get started!",
                R.drawable.onboardone, false));

        fragmentList.add(OnboardingFragment.newInstance("Discover Nearby Parking",
                "Finding a spot is a breeze. Browse nearby parking locations and get to your destination faster, whether you’re a user or an owner.",
                R.drawable.onboardtwo, false));

        fragmentList.add(OnboardingFragment.newInstance("Pay and Park with Ease",
                "Enjoy a smooth, secure payment experience. No matter if you're parking or providing a spot, managing payments has never been easier.",
                R.drawable.onboardthree, true));


        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
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
