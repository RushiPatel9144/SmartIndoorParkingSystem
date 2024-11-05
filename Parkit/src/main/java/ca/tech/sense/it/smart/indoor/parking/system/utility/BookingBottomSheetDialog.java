package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class BookingBottomSheetDialog extends BottomSheetDialog {

    private DatabaseReference locationRef;
    private Context context;
    private TextView userNameTextView;
    private AppCompatSpinner timeSlotSpinner;
    private Button confirmButton, cancelButton;
    private ProgressBar progressBar;
    private TextView addressText;
    private TextView postalCodeText;

    public BookingBottomSheetDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_booking, null);
        setContentView(view);

        userNameTextView = view.findViewById(R.id.userNameTextView);
        timeSlotSpinner = view.findViewById(R.id.timeSlotSpinner);
        confirmButton = view.findViewById(R.id.confirmButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        progressBar = view.findViewById(R.id.progressBar);
        addressText=view.findViewById(R.id.addressText);
        postalCodeText=view.findViewById(R.id.postalCodeText);

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        locationRef = FirebaseDatabase.getInstance().getReference("parkingLocations").child(userId).child("address");
        fetchUserInfo();
        setupTimeSlots();
        setupConfirmButton();
        setupCancelButton(); // Setup the cancel button
    }

    private void fetchUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            fetchUserName(uid);
        }
    }

    // Method to fetch the user's first name from Firestore
    private void fetchUserName(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String firstName = document.getString("firstName");
                    if (firstName != null) {
                        userNameTextView.setText("Hey! "+ firstName);
                    }
                }
            }
        });

    }

    private void setupTimeSlots() {
        // Populate spinner with time slots
        String[] timeSlots = {"9:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM", "1:00 PM - 2:00 PM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem().toString();
            setupProceedToPayment(selectedTimeSlot);
        });
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> {
            dismiss(); // Dismiss the bottom sheet when the cancel button is clicked
        });
    }

    private void setupProceedToPayment(String timing) {
        confirmButton.setOnClickListener(v -> {
            // Assuming you have a way to get the date, here just creating a new Date
            Date selectedDate = new Date(); // Replace this with your actual date selection logic

            if (!timing.isEmpty()) {
                processPayment(timing);
            } else {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPayment(String selectedTimeSlot) {
        // Simulate payment processing
        progressBar.setVisibility(View.VISIBLE);
        confirmButton.setEnabled(false);
        // Replace with actual payment logic
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            confirmButton.setEnabled(true);
            dismiss();
            Toast.makeText(context, "Booking confirmed for " + selectedTimeSlot, Toast.LENGTH_SHORT).show();
        }, 2000); // Simulate a 2-second delay for processing payment
    }
}
