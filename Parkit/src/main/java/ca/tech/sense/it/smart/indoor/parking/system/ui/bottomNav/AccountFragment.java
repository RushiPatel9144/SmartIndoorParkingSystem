package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.ManageAccountFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.NotificationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.PrivatePolicyFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.RateUsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.SettingsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.ui.AccountItems.TermsOfUseFragment;

public class AccountFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccountAdapter accountAdapter;
    private ArrayList<String> accountOptions;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        accountOptions = new ArrayList<>();
        accountOptions.add("Manage Account");
        accountOptions.add("Settings");
        accountOptions.add("Help");
        accountOptions.add("Rate Us");
        accountOptions.add("Notification");
        accountOptions.add("Privacy Policy");
        accountOptions.add("Terms of Service");
        accountOptions.add("Logout");

        // Set up the adapter with click listener
        accountAdapter = new AccountAdapter(accountOptions, this::onAccountOptionSelected);
        recyclerView.setAdapter(accountAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        return view;
    }

    private void onAccountOptionSelected(int position) {
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new ManageAccountFragment();
                break;
            case 1:
                fragment = new SettingsFragment();
                break;
            case 2:
                fragment = new HelpFragment();
                break;
            case 3:
                fragment = new RateUsFragment();
                break;
            case 4:
                fragment = new NotificationsFragment();
                break;
            case 5:
                fragment = new PrivatePolicyFragment();
                break;
            case 6:
                fragment = new TermsOfUseFragment();
                break;
            case 7:
                // Handle logout here, if needed
                break;
        }

        // Check if fragment is not null before replacing
        if (fragment != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, fragment); // Ensure the container ID is correct
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}
