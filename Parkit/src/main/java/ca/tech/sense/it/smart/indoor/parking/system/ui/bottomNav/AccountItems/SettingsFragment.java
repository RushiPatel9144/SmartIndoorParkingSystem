package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class SettingsFragment extends Fragment {

    private Switch switchLockPortrait;
    private Switch switchNotifications;
    private Switch switchTheme;
    private Spinner spinnerCurrency;

    public SettingsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize UI elements
        switchLockPortrait = view.findViewById(R.id.switch_lock_portrait);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchTheme = view.findViewById(R.id.switch_theme);
        spinnerCurrency = view.findViewById(R.id.spinner_currency);

        // Load saved preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean isPortraitLocked = sharedPreferences.getBoolean(getString(R.string.lock_portrait), false);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.dark_theme), false);
        boolean areNotificationsEnabled = sharedPreferences.getBoolean("Notification Enabled", true);

        switchLockPortrait.setChecked(isPortraitLocked);
        switchTheme.setChecked(isDarkTheme);
        switchNotifications.setChecked(areNotificationsEnabled);

        // Set up listeners
        switchLockPortrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.lock_portrait), isChecked);
                editor.apply();

                if (isChecked) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        });

        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.dark_theme), isChecked);
                editor.apply();

                applyTheme(isChecked);

                // Refresh the fragment to apply the theme change
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
                ft.detach(SettingsFragment.this).attach(SettingsFragment.this).commit();
            }
        });

        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Notification Enabled", isChecked);
                editor.apply();

                if (isChecked) {
                    // Enable notifications
                    enableNotifications();
                } else {
                    // Disable notifications
                    disableNotifications();
                }
            }
        });

        // Initialize spinner with currency options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.currency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        return view;
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void enableNotifications() {
        // Logic to enable notifications
    }

    private void disableNotifications() {
        // Logic to disable notifications
    }
}
