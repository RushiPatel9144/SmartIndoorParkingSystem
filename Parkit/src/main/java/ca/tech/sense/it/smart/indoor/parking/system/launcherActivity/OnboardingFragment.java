package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class OnboardingFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE_RES_ID = "imageResId";
    private static final String ARG_IS_LAST_PAGE = "isLastPage";

    private String title;
    private String description;
    private int imageResId;
    private boolean isLastPage;

    public OnboardingFragment() {
        // Required empty public constructor
    }

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
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);
            isLastPage = getArguments().getBoolean(ARG_IS_LAST_PAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        TextView titleTextView = view.findViewById(R.id.onBoard_titleTextView);
        TextView descriptionTextView = view.findViewById(R.id.onBoard_descriptionTextView);
        ImageView imageView = view.findViewById(R.id.onBoard_imageView);
        Button getStartedButton = view.findViewById(R.id.onBoard_getStartedButton);
        ProgressBar progressBar = view.findViewById(R.id.onBoard_progressBar); // Get ProgressBar from fragment


        titleTextView.setText(title);
        descriptionTextView.setText(description);
        imageView.setImageResource(imageResId);
        progressBar.setProgress(0);


        // Show "Get Started" button only if this is the last page
        if (isLastPage) {
            getStartedButton.setVisibility(View.VISIBLE);
            getStartedButton.setOnClickListener(v -> {
                // Set onboarding completed in SharedPreferences
                SharedPreferences preferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
                preferences.edit().putBoolean("isFirstTime", false).apply();

                // Navigate to FirstActivity
                startActivity(new Intent(getActivity(), FirstActivity.class));
                requireActivity().finish();
            });
        }

        return view;
    }

    public void updateProgress(int progress) {
        ProgressBar progressBar = requireView().findViewById(R.id.onBoard_progressBar);
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }
}
