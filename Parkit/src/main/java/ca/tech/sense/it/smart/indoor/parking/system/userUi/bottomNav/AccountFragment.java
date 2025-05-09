/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.CoroutineHelper;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle.GoogleAuthClient;
import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.ui.FirstActivity;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.NotificationsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.PrivatePolicyFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.RateUsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.SettingsFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.TermsOfUseFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.manageAccount.ManageAccountFragment;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class AccountFragment extends Fragment {
    private static final String ARG_CONTAINER_VIEW_ID = "containerViewId";
    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";

    private int containerViewId;
    SessionManager sessionManager;
    ImageView profilePic;
    private static final String TAG = "AccountFragment";

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance(int containerViewId) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CONTAINER_VIEW_ID, containerViewId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            containerViewId = getArguments().getInt(ARG_CONTAINER_VIEW_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        sessionManager = SessionManager.getInstance(requireContext());
        profilePic = view.findViewById(R.id.accountFrag_ProfilePic);

        setupClickListeners(view);
        loadProfilePictureLocally();
        return view;
    }

    private void loadProfilePictureLocally() {
        SharedPreferences sharedPreferences;
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String photoUrl = sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null);

        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profilePic);
        }
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
        fragmentTransaction.replace(containerViewId, fragment, TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void handleLogout() {
        DialogUtil.showLeaveAppDialog(requireContext(), getString(R.string.confirm_logout),getString(R.string.are_you_sure_you_want_to_log_out), R.drawable.crisis,
                new DialogUtil.BackPressCallback() {
                    @Override
                    public void onConfirm() {

                        GoogleAuthClient googleAuthClient = new GoogleAuthClient(requireContext());

                        FirebaseAuthSingleton.getInstance().signOut();
                        sessionManager.logout();
                        SharedPreferences rateUsSharedPreferences = requireContext().getSharedPreferences("RateUsPrefs", Context.MODE_PRIVATE);
                        rateUsSharedPreferences.edit().clear().apply();
                        SharedPreferences accountSharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        accountSharedPreferences.edit().clear().apply();

                        if (googleAuthClient.isSingedIn()){
                            CoroutineHelper.Companion.signOutWithGoogle(requireContext(), googleAuthClient, () -> {});
                        }


                        Intent intent = new Intent(requireActivity(), FirstActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }

                    @Override
                    public void onCancel() {
                        //dismiss
                    }
                });
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

