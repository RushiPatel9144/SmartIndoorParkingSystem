/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LoginActivity;
import ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.*;

public class AccountFragment extends Fragment {

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        setupClickListeners(view);

        return view;
    }

    private void setupClickListeners(View view) {
        setSectionClickListener(view, R.id.manage_account, AccountSection.MANAGE_ACCOUNT);
        setSectionClickListener(view, R.id.settings, AccountSection.SETTINGS);
        setSectionClickListener(view, R.id.help, AccountSection.HELP);
        setSectionClickListener(view, R.id.rate, AccountSection.RATE_US);
        setSectionClickListener(view, R.id.notification, AccountSection.NOTIFICATIONS);
        setSectionClickListener(view, R.id.privacy, AccountSection.PRIVACY_POLICY);
        setSectionClickListener(view, R.id.terms, AccountSection.TERMS_OF_USE);
        setSectionClickListener(view, R.id.logout, AccountSection.LOGOUT);
    }

    private void setSectionClickListener(View view, int layoutId, AccountSection section) {
        LinearLayout layout = view.findViewById(layoutId);
        layout.setOnClickListener(v -> handleSectionSelection(section));
    }

    private void handleSectionSelection(AccountSection section) {
        switch (section) {
            case MANAGE_ACCOUNT:
                openFragment(new ManageAccountFragment());
                break;
            case SETTINGS:
                openFragment(new SettingsFragment());
                break;
            case HELP:
                openFragment(new HelpFragment());
                break;
            case RATE_US:
                openFragment(new RateUsFragment());
                break;
            case NOTIFICATIONS:
                openFragment(new NotificationsFragment());
                break;
            case PRIVACY_POLICY:
                openFragment(new PrivatePolicyFragment());
                break;
            case TERMS_OF_USE:
                openFragment(new TermsOfUseFragment());
                break;
            case LOGOUT:
                handleLogout();
                break;
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void handleLogout() {
        // Clear the in-memory session data
//        UserSession.clearSession();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private enum AccountSection {
        MANAGE_ACCOUNT,
        SETTINGS,
        HELP,
        RATE_US,
        NOTIFICATIONS,
        PRIVACY_POLICY,
        TERMS_OF_USE,
        LOGOUT
    }
}
