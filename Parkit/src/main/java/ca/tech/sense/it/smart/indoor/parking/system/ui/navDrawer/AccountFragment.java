package ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class AccountFragment extends Fragment {


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Bind views
        ImageView profilePicture = view.findViewById(R.id.profile_picture);
        TextView name = view.findViewById(R.id.name);
        TextView contactDetails = view.findViewById(R.id.contact_details);

        // Set data (replace with actual data)
        profilePicture.setImageResource(R.drawable.ic_profile_placeholder); // Replace with actual image resource or URL
        name.setText("The TECH SENSE"); // Replace with actual name
        contactDetails.setText("thetechsense123@gmail.com"); // Replace with actual contact details

        return view;
    }
}