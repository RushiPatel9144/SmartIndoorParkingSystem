package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.RateUsViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.utility.RateUsViewModelFactory;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class RateUsFragment extends BaseNetworkFragment implements RateUsViewModel.FeedbackSubmissionCallback {

    RatingBar ratingBar;
    EditText feedbackComment;
    Button submitFeedbackButton;
    TextView optionParkingSpot, optionSecureTransaction, optionUserInterface, optionRealTime;
    ProgressBar progressBar;
    List<String> selectedOptions;
    RateUsViewModel rateUsViewModel;

    public RateUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedOptions = new ArrayList<>();
        RateUsViewModelFactory factory = new RateUsViewModelFactory(requireContext());
        rateUsViewModel = new ViewModelProvider(this, factory).get(RateUsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_us, container, false);

        // Initialize UI components
        initializeUIComponents(view);

        // Check if the user can submit feedback
        long lastSubmissionTime = rateUsViewModel.getLastSubmissionTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSubmissionTime < rateUsViewModel.getTwentyFourHours()) {
            long remainingTime = rateUsViewModel.getTwentyFourHours() - (currentTime - lastSubmissionTime);
            startTimer(remainingTime);
            submitFeedbackButton.setEnabled(false);
            submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        }

        // Set up click listener for feedback submission
        setupSubmitButtonClickListener();

        return view;
    }

    // Initializes UI components by linking them to the XML layout elements.
    private void initializeUIComponents(View view) {
        ratingBar = view.findViewById(R.id.rating_bar);
        feedbackComment = view.findViewById(R.id.feedback_comment);
        submitFeedbackButton = view.findViewById(R.id.submit_feedback_button);
        optionParkingSpot = view.findViewById(R.id.option_parking_spot);
        optionSecureTransaction = view.findViewById(R.id.option_secure_transaction);
        optionUserInterface = view.findViewById(R.id.option_user_interface);
        optionRealTime = view.findViewById(R.id.option_real_time);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set up option click listeners
        setOptionClickListener(optionParkingSpot, getString(R.string.parking_spot_easily_found));
        setOptionClickListener(optionSecureTransaction, getString(R.string.secure_transaction));
        setOptionClickListener(optionUserInterface, getString(R.string.user_friendly_interface));
        setOptionClickListener(optionRealTime, getString(R.string.real_time_features));
    }

    private void setupSubmitButtonClickListener() {
        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get rating value
                float rating = ratingBar.getRating();
                // Get feedback comment
                String comment = feedbackComment.getText().toString();

                // Check if required fields are filled
                if (rating == 0) {
                    Toast.makeText(getContext(), "Please provide a rating.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get device model
                String deviceModel = Build.MODEL;
                String manufacturer = Build.MANUFACTURER;
                String fullDeviceInfo = manufacturer + " " + deviceModel;

                // Submit feedback
                rateUsViewModel.submitFeedback(rating, comment, selectedOptions, fullDeviceInfo, getContext(), RateUsFragment.this, () -> {
                    // Clear the inputs after submission
                    ratingBar.setRating(0);
                    feedbackComment.setText("");
                    clearSelectedOptions();
                    // Show confirmation dialog
                    DialogUtil.showConfirmationDialog(getActivity(), "Confirmation", getString(R.string.feedback_submitted_successfully), getString(R.string.ok), new DialogUtil.ConfirmDialogCallback() {
                        @Override
                        public void onConfirm() {
                            // Dialog will be dismissed automatically
                        }
                    });
                    // Disable the submit button and start the timer
                    submitFeedbackButton.setEnabled(false);
                    submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                    startTimer(rateUsViewModel.getTwentyFourHours());
                }, () -> {
                    Toast.makeText(getContext(), getString(R.string.error_submitting_feedback), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        submitFeedbackButton.setVisibility(View.GONE);

        // Hide progress bar after 5 seconds
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // No action needed on tick
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                submitFeedbackButton.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        submitFeedbackButton.setVisibility(View.VISIBLE);
    }

    private void setOptionClickListener(TextView optionView, String optionText) {
        optionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedOptions.contains(optionText)) {
                    selectedOptions.remove(optionText);
                    optionView.setBackgroundResource(R.drawable.box_background);
                } else {
                    selectedOptions.add(optionText);
                    optionView.setBackgroundResource(R.drawable.box_background_selected);
                }
            }
        });
    }

    private void clearSelectedOptions() {
        selectedOptions.clear();
        optionParkingSpot.setBackgroundResource(R.drawable.box_background);
        optionSecureTransaction.setBackgroundResource(R.drawable.box_background);
        optionUserInterface.setBackgroundResource(R.drawable.box_background);
        optionRealTime.setBackgroundResource(R.drawable.box_background);
    }

    private void startTimer(long duration) {
        new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                submitFeedbackButton.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                submitFeedbackButton.setEnabled(true);
                submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme));
                submitFeedbackButton.setText(R.string.submit_feedback);
            }
        }.start();
    }
}
