package ca.tech.sense.it.smart.indoor.parking.system;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import android.view.View;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login.LoginActivity;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testLogin() {
        // Type email and password
        onView(withId(R.id.login_email_editext)).perform(typeText("aaa@bbb.com"));
        onView(withId(R.id.login_password_editext)).perform(typeText("Admin101!"));

        // Click the login button
        onView(withId(R.id.login_btn)).perform(click());

        // Custom ViewAction to wait for the UI to be idle
        onView(isRoot()).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for UI to be idle";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
            }
        });

        // Check if the progress bar is displayed
        onView(withId(R.id.login_progressBar)).check(matches(isDisplayed()));
    }

    @Test
    public void testGoogleSignInButton() {
        // Verify the Google Sign-In button is displayed
        onView(withId(R.id.btnGoogleSignIn)).check(matches(isDisplayed()));

        // Click the Google Sign-In button
        onView(withId(R.id.btnGoogleSignIn)).perform(click());

        // Custom ViewAction to wait for the UI to be idle
        onView(isRoot()).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for UI to be idle";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
            }
        });

        // Additional checks can be added here to verify Google Sign-In flow behavior
    }
}