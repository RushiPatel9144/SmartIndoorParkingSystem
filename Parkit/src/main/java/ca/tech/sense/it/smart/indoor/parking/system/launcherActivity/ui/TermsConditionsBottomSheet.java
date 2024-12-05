package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.TermsOfUseFragment;

public class TermsConditionsBottomSheet extends BottomSheetDialogFragment {

    // Interface to handle acceptance
    public interface TermsDialogListener {
        void onTermsAccepted();  // This method will be called when the terms are accepted
    }

    private TermsDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (TermsDialogListener) context;  // Ensure the activity implements the listener
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TermsDialogListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the bottom sheet layout
        View view = inflater.inflate(R.layout.fragment_terms_conditions_bottom_sheet, container, false);

        // Dynamically add the existing fragment into the BottomSheet
        if (getChildFragmentManager().findFragmentByTag("existing_fragment_tag") == null) {
            TermsOfUseFragment termsOfUseFragment = new TermsOfUseFragment(); // Your existing fragment
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.dialogfragmentContainer, termsOfUseFragment, "existing_fragment_tag");  // Use the container inside the BottomSheet
            transaction.commit();
        }

        // Initialize the buttons
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialButton btnAccept = view.findViewById(R.id.dialogbtnAccept);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialButton btnDecline = view.findViewById(R.id.dialogbtnDecline);

        // Accept button click listener
        btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTermsAccepted();  // Notify activity that terms are accepted
            }
            dismiss();  // Close the dialog
        });

        // Decline button click listener
        btnDecline.setOnClickListener(v -> dismiss());  // Close dialog on decline

        return view;
    }
}
