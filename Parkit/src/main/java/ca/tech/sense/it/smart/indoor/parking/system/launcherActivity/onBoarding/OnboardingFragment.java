package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.onBoarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.FirstActivity;

/**
 * Fragment for onboarding screens with dynamic content.
 */
public class OnboardingFragment extends Fragment {

    // Keys for arguments
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE_RES_ID = "imageResId";
    private static final String ARG_IS_LAST_PAGE = "isLastPage";

    // Instance variables
    private String title;
    private String description;
    private int imageResId;
    private boolean isLastPage;
    private LinearLayout swipeLayout;

    public OnboardingFragment() {
        // Default constructor
    }

    /**
     * Creates a new instance of the onboarding fragment with specified arguments.
     *
     * @param title       The title text for the onboarding screen.
     * @param description The description text for the onboarding screen.
     * @param imageResId  The resource ID of the image to display.
     * @param isLastPage  Whether this is the last onboarding page.
     * @return A configured instance of OnboardingFragment.
     */
    public static OnboardingFragment newInstance(String title, String description, int imageResId, boolean isLastPage) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_IMAGE_RES_ID, imageResId);
        args.putBoolean(ARG_IS_LAST_PAGE, isLastPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve arguments if available
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);
            isLastPage = getArguments().getBoolean(ARG_IS_LAST_PAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        // Initialize views
        initializeViews(view);

        return view;
    }

    /**
     * Initializes the UI components of the fragment.
     *
     * @param view The root view of the fragment.
     */
    private void initializeViews(View view) {
        TextView titleTextView = view.findViewById(R.id.onBoard_titleTextView);
        TextView descriptionTextView = view.findViewById(R.id.onBoard_descriptionTextView);
        ImageView imageView = view.findViewById(R.id.onBoard_imageView);
        Button getStartedButton = view.findViewById(R.id.onBoard_getStartedButton);

        swipeLayout = view.findViewById(R.id.onBoard_swipe);
        // Set the content for the fragment
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        imageView.setImageResource(imageResId);

        // Configure the "Get Started" button for the last page
        configureGetStartedButton(getStartedButton,swipeLayout);
        swipeLayout.setOnClickListener(v -> navigateToNextFragment());
    }

    private void navigateToNextFragment() {

        if (getActivity() instanceof OnboardingActivity) {
            OnboardingActivity activity = (OnboardingActivity) getActivity();
            int currentItem = activity.getViewPager().getCurrentItem();
            int totalItems = activity.getFragmentList().size();

            if (currentItem < totalItems - 1) {
                // Navigate to the next page
                activity.getViewPager().setCurrentItem(currentItem + 1, true);
            }
        }
    }

    /**
     * Configures the "Get Started" button. Only visible and functional on the last page.
     *
     * @param getStartedButton The button to be configured.
     */
    private void configureGetStartedButton(Button getStartedButton,LinearLayout swipeLayout) {
        if (isLastPage) {
            swipeLayout.setVisibility(View.GONE);
            getStartedButton.setVisibility(View.VISIBLE);
            getStartedButton.setOnClickListener(v -> completeOnboarding());
        } else {
            getStartedButton.setVisibility(View.GONE);
            swipeLayout.setVisibility(View.VISIBLE);
        }
    }



    /**
     * Marks onboarding as complete and navigates to the first activity.
     */
    private void completeOnboarding() {
        // Update shared preferences to indicate onboarding completion
        SharedPreferences preferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("isFirstTime", false).apply();

        // Navigate to the first activity
        startActivity(new Intent(getActivity(), FirstActivity.class));
        requireActivity().finish();
    }
}
