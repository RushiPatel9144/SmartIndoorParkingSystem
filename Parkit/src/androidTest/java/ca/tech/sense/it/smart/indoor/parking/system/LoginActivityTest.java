package ca.tech.sense.it.smart.indoor.parking.system;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login.LoginActivity;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testLogin() {
        // Type email and password
        onView(withId(R.id.login_email_editext)).perform(typeText("test@example.com"));
        onView(withId(R.id.login_password_editext)).perform(typeText("password"));

        // Click the login button
        onView(withId(R.id.login_btn)).perform(click());

        // Check if the progress bar is displayed
        onView(withId(R.id.login_progressBar)).check(matches(isDisplayed()));
    }
}
