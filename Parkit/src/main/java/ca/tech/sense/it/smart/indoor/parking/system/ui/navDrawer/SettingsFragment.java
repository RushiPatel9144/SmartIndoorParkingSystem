package ca.tech.sense.it.smart.indoor.parking.system.ui.navDrawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.ToggleButton;
import androidx.fragment.app.Fragment;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class SettingsFragment extends Fragment {

    private Switch switchLockPortrait;
    private Switch switchNotifications;
    private ToggleButton toggleTheme;
    private Spinner spinnerLanguage;

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
        toggleTheme = view.findViewById(R.id.toggle_theme);
        spinnerLanguage = view.findViewById(R.id.spinner_language);

        // Load saved preferences
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean isPortraitLocked = sharedPreferences.getBoolean("lock_portrait", false);
        boolean isDarkTheme = sharedPreferences.getBoolean("dark_theme", false);

        switchLockPortrait.setChecked(isPortraitLocked);
        toggleTheme.setChecked(isDarkTheme);

        // Set up listeners
        switchLockPortrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("lock_portrait", isChecked);
                editor.apply();

                if (isChecked) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        });

        toggleTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("dark_theme", isChecked);
                editor.apply();

                // Apply theme change logic here
                if (isChecked) {
                    // Set dark theme
                } else {
                    // Set light theme
                }
            }
        });

        // Other initialization code here

        return view;
    }
}
