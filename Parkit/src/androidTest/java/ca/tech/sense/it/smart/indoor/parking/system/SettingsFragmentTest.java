package ca.tech.sense.it.smart.indoor.parking.system;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.is;

import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.SettingsFragment;

@RunWith(AndroidJUnit4.class)
public class SettingsFragmentTest {

    @Before
    public void setUp() {
        FragmentScenario.launchInContainer(SettingsFragment.class);
    }

    @Test
    public void testSwitchLockPortrait() {
        onView(withId(R.id.switch_lock_portrait))
                .perform(click())
                .check(matches(isChecked()));

        onView(withId(R.id.switch_lock_portrait))
                .perform(click())
                .check(matches(isNotChecked()));
    }

    @Test
    public void testSwitchNotifications() {
        // Ensure the switch is checked initially
        onView(withId(R.id.switch_notifications))
                .check(matches(isChecked()));

        // Perform click to uncheck the switch
        onView(withId(R.id.switch_notifications))
                .perform(click())
                .check(matches(isNotChecked()));

        // Perform click to check the switch again
        onView(withId(R.id.switch_notifications))
                .perform(click())
                .check(matches(isChecked()));
    }


    @Test
    public void testSwitchTheme() {
        onView(withId(R.id.switch_theme))
                .perform(click())
                .check(matches(isChecked()));

        onView(withId(R.id.switch_theme))
                .perform(click())
                .check(matches(isNotChecked()));
    }

}
