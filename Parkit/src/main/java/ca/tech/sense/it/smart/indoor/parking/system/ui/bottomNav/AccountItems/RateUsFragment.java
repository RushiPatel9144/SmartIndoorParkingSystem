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
import androidx.lifecycle.ViewModelProvider;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.network.BaseNetworkFragment;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.RateUsViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.utility.RateUsViewModelFactory;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class RateUsFragment extends BaseNetworkFragment implements RateUsViewModel.FeedbackSubmissionCallback {

    RatingBar ratingBar;
    private CountDownTimer countDownTimer;
    EditText feedbackComment;
    Button submitFeedbackButton;
    TextView optionParkingSpot;
    TextView optionSecureTransaction;
    TextView optionUserInterface;
    TextView optionRealTime;
    ProgressBar progressBar;
    List<String> selectedOptions = new ArrayList<>();
    RateUsViewModel rateUsViewModel;

    public RateUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rateUsViewModel = new ViewModelProvider(this, new RateUsViewModelFactory(requireContext())).get(RateUsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rate_us, container, false);
        initializeUIComponents(view);
        configureSubmitButton();
        checkLastSubmissionTime();
        return view;
    }

    private void initializeUIComponents(View view) {
        ratingBar = view.findViewById(R.id.rating_bar);
        feedbackComment = view.findViewById(R.id.feedback_comment);
        submitFeedbackButton = view.findViewById(R.id.submit_feedback_button);
        optionParkingSpot = view.findViewById(R.id.option_parking_spot);
        optionSecureTransaction = view.findViewById(R.id.option_secure_transaction);
        optionUserInterface = view.findViewById(R.id.option_user_interface);
        optionRealTime = view.findViewById(R.id.option_real_time);
        progressBar = view.findViewById(R.id.progress_bar);

        setupOptionClickListeners();
    }

    private void setupOptionClickListeners() {
        setOptionClickListener(optionParkingSpot, getString(R.string.parking_spot_easily_found));
        setOptionClickListener(optionSecureTransaction, getString(R.string.secure_transaction));
        setOptionClickListener(optionUserInterface, getString(R.string.user_friendly_interface));
        setOptionClickListener(optionRealTime, getString(R.string.real_time_features));
    }

    private void setOptionClickListener(TextView optionView, String optionText) {
        optionView.setOnClickListener(v -> toggleOptionSelection(optionView, optionText));
    }

    private void toggleOptionSelection(TextView optionView, String optionText) {
        boolean isSelected = selectedOptions.contains(optionText);
        if (isSelected) {
            selectedOptions.remove(optionText);
            updateOptionBackground(optionView, false);
        } else {
            selectedOptions.add(optionText);
            updateOptionBackground(optionView, true);
        }
    }

    private void updateOptionBackground(TextView optionView, boolean isSelected) {
        optionView.setBackgroundResource(isSelected ? R.drawable.box_background_selected : R.drawable.box_background);
        optionView.setTextColor(isSelected ? ContextCompat.getColor(requireContext(), R.color.white): ContextCompat.getColor(requireContext(), R.color.black));
    }

    private void configureSubmitButton() {
        submitFeedbackButton.setOnClickListener(v -> {
            if (validateInput()) {
                submitFeedback();
            }
        });
    }

    private boolean validateInput() {
        if (ratingBar.getRating() == 0) {
            Toast.makeText(getContext(), R.string.provide_rating_message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comment = feedbackComment.getText().toString();
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL;

        rateUsViewModel.submitFeedback(rating, comment, selectedOptions, deviceInfo, getContext(), this, this::onFeedbackSubmitted, this::onFeedbackError);
    }

    private void onFeedbackSubmitted() {
        clearInputFields();
        DialogUtil.showConfirmationDialog(getActivity(), getString(R.string.confirmation_title), getString(R.string.feedback_submitted_successfully), getString(R.string.ok), null);
        startCooldownTimer(rateUsViewModel.getTwentyFourHours());
    }

    private void onFeedbackError() {
        Toast.makeText(getContext(), R.string.error_submitting_feedback, Toast.LENGTH_SHORT).show();
    }

    private void clearInputFields() {
        ratingBar.setRating(0);
        feedbackComment.setText("");
        clearSelectedOptions();
    }

    private void clearSelectedOptions() {
        selectedOptions.clear();
        updateOptionBackground(optionParkingSpot, false);
        updateOptionBackground(optionSecureTransaction, false);
        updateOptionBackground(optionUserInterface, false);
        updateOptionBackground(optionRealTime, false);
    }

    private void checkLastSubmissionTime() {
        long lastSubmissionTime = rateUsViewModel.getLastSubmissionTime();
        long currentTime = System.currentTimeMillis();
        long remainingTime = rateUsViewModel.getTwentyFourHours() - (currentTime - lastSubmissionTime);

        if (remainingTime > 0) {
            startCooldownTimer(remainingTime);
        }
    }

    private void startCooldownTimer(long duration) {
        // Cancel any existing timer before starting a new one
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        submitFeedbackButton.setEnabled(false);
        submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isAdded()) { // Check if the fragment is attached before accessing UI or context
                    submitFeedbackButton.setText(MessageFormat.format("{0}{1}", getString(R.string.submit_again_in), formatTime(millisUntilFinished)));
                }
            }

            @Override
            public void onFinish() {
                if (isAdded()) { // Ensure fragment is still attached before updating UI
                    enableSubmitButton();
                }
            }
        }.start();
    }


    private void enableSubmitButton() {
        submitFeedbackButton.setEnabled(true);
        submitFeedbackButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.theme));
        submitFeedbackButton.setText(R.string.submit_feedback);
    }

    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        submitFeedbackButton.setVisibility(View.GONE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        submitFeedbackButton.setVisibility(View.VISIBLE);
    }
}
