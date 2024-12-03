package ca.tech.sense.it.smart.indoor.parking.system;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
        disableAnimations();

        // Create the intent and set the required extras
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PaymentActivity.class);
        intent.putExtra("ownerId", "testOwnerId");

        // Mock Booking object
        Booking mockBooking = new Booking();
        mockBooking.setCurrencyCode("USD");
        mockBooking.setTitle("Parking Name");
        mockBooking.setLocation("Address");
        mockBooking.setPostalCode("Postal Code");
        intent.putExtra("booking", mockBooking);

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
                .perform(typeText("PROMO123"));
        onView(withId(R.id.applyPromoCodeButton))
                .perform(click());
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

    private void disableAnimations() {

    }
}
