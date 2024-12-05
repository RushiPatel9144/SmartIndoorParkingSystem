package ca.tech.sense.it.smart.indoor.parking.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.FirstActivity;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.login.LoginActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29) // Specify the API level to simulate
public class FirstActivityTest {

    private FirstActivity activity;

    @Before
    public void setUp() {
        // Initialize Firebase
        FirebaseApp.initializeApp(RuntimeEnvironment.application);

        // Build and set up the activity
        ActivityController<FirstActivity> controller = Robolectric.buildActivity(FirstActivity.class);
        controller.setup();
        activity = controller.get();
    }

    @Test
    public void clickingLogin_shouldStartLoginActivityForUser() {
        MaterialButton signInButton = activity.findViewById(R.id.firstSignInBut);
        performLoginTest(signInButton, "user");
    }

    @Test
    public void clickingLogin_shouldStartLoginActivityForOwner() {
        MaterialButton signInButton = activity.findViewById(R.id.firstSignInAsOwnerBut);
        performLoginTest(signInButton, "owner");
    }

    private void performLoginTest(MaterialButton signInButton, String expectedUserType) {
        signInButton.performClick();

        // Testing Intent
        Intent actual = shadowOf(activity).getNextStartedActivity();
        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        assertEquals(expectedIntent.getComponent(), actual.getComponent());

        // Testing intent extra for user/owner
        String actualUserType = actual.getStringExtra("userType");
        assertEquals(expectedUserType, actualUserType);
    }


}
