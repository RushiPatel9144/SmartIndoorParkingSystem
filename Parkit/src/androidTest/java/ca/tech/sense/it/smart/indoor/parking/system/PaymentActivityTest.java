package ca.tech.sense.it.smart.indoor.parking.system;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import android.util.Log;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import ca.tech.sense.it.smart.indoor.parking.system.booking.PaymentActivity;
import ca.tech.sense.it.smart.indoor.parking.system.model.booking.Booking;

@RunWith(AndroidJUnit4.class)
public class PaymentActivityTest {

    @Rule
    public ActivityScenarioRule<PaymentActivity> activityRule =
            new ActivityScenarioRule<>(PaymentActivity.class);

    private IdlingResource idlingResource;

    @Before
    public void setUp() {
        // Disable animations on the device
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        // Create the intent and set the required extras
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PaymentActivity.class);
        intent.putExtra("ownerId", "testOwnerId");

        // Initialize the Booking object
        Booking booking = new Booking();
        booking.setCurrencyCode("USD");
        booking.setTitle("Parking Name");
        booking.setLocation("Address");
        booking.setPostalCode("Postal Code");
        intent.putExtra("booking", booking);

        // Launch the activity with the intent
        ActivityScenario<PaymentActivity> scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        // Unregister IdlingResource if needed
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

    @Test
    public void testBookingDetailsDisplayed() {
        onView(withId(R.id.parkingNameTextView))
                .check(matches(withText("Parking Name")));
        onView(withId(R.id.addressTextView))
                .check(matches(withText("Address")));
        onView(withId(R.id.postalCodeTextView))
                .check(matches(withText("Postal Code")));
    }

    @Test
    public void testApplyPromoCode() {
        onView(withId(R.id.promoCodeEditText))
                .perform(replaceText("")); // Clear existing text

        // Wait for manual input
        onView(isRoot()).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for input";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(100); //
            }
        });

        // Click the apply promo code button
        onView(withId(R.id.applyPromoCodeButton)).perform(click());

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
    }

    @Test
    public void testUIElementsVisibility() {
        // Verify if essential UI elements are displayed
        onView(withId(R.id.parkingNameTextView))
                .check(matches(isDisplayed()));
        onView(withId(R.id.addressTextView))
                .check(matches(isDisplayed()));
        onView(withId(R.id.postalCodeTextView))
                .check(matches(isDisplayed()));
        onView(withId(R.id.promoCodeEditText))
                .check(matches(isDisplayed()));
        onView(withId(R.id.applyPromoCodeButton))
                .check(matches(isDisplayed()));
        onView(withId(R.id.confirmButton))
                .check(matches(isDisplayed()));
        onView(withId(R.id.cancelButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testConfirmButton() {
        onView(withId(R.id.confirmButton))
                .perform(click());
    }

    @Test
    public void testCancelButton() {
        onView(withId(R.id.cancelButton))
                .perform(click());
    }
}
