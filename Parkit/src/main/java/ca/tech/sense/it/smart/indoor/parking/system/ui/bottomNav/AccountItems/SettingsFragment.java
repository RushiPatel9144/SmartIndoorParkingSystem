/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.Manager.NotificationManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.Manager.PreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.currency.Currency;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyPreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.ThemeViewModel;

public class SettingsFragment extends Fragment {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchLockPortrait;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchNotifications;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchTheme;
    private Spinner spinnerCurrency;
    private ThemeViewModel themeViewModel;
    private PreferenceManager preferenceManager;
    private NotificationManagerHelper notificationManagerHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeViewModel = new ViewModelProvider(requireActivity()).get(ThemeViewModel.class);
        preferenceManager = new PreferenceManager(requireContext());
        notificationManagerHelper = new NotificationManagerHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeUIElements(view);
        loadPreferences();
        observeThemeChanges();
        setupListeners();
        initializeSpinner();

        return view;
    }

    private void initializeUIElements(View view) {
        switchLockPortrait = view.findViewById(R.id.switch_lock_portrait);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchTheme = view.findViewById(R.id.switch_theme);
        spinnerCurrency = view.findViewById(R.id.spinner_currency);
    }

    private void loadPreferences() {
        switchLockPortrait.setChecked(preferenceManager.isPortraitLocked());
        switchTheme.setChecked(preferenceManager.isDarkTheme());
        switchNotifications.setChecked(preferenceManager.areNotificationsEnabled());
    }

    private void observeThemeChanges() {
        themeViewModel.getIsDarkTheme().observe(getViewLifecycleOwner(), isDarkTheme -> {
            applyTheme(isDarkTheme);
            switchTheme.setChecked(isDarkTheme);
        });
    }

    private void setupListeners() {
        switchLockPortrait.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setPortraitLocked(isChecked);
            requireActivity().setRequestedOrientation(isChecked ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        });

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeViewModel.setIsDarkTheme(isChecked);
            preferenceManager.setDarkTheme(isChecked);
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setNotificationsEnabled(isChecked);
            if (isChecked) {
                notificationManagerHelper.enableNotifications();
            } else {
                notificationManagerHelper.disableNotifications();
            }
        });
    }

    private void initializeSpinner() {
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        Map<String, Currency> currencies = currencyManager.getCurrencies();

        // Extract currency codes into a list for displaying in the spinner
        List<String> currencyNames = new ArrayList<>(currencies.keySet());

        // Create and set the ArrayAdapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, currencyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        // Get the stored currency from SharedPreferences
        CurrencyPreferenceManager currencyPreferenceManager = new CurrencyPreferenceManager(requireActivity());
        String storedCurrencyCode = currencyPreferenceManager.getSelectedCurrency();

        // Set the spinner's selected item to the stored currency (if exists)
        int storedCurrencyPosition = currencyNames.indexOf(storedCurrencyCode);
        if (storedCurrencyPosition != -1) {
            spinnerCurrency.setSelection(storedCurrencyPosition);
        }
        // Handle item selection from the spinner
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get selected currency code from the spinner and retrieve the corresponding currency object
                String selectedCurrencyCode = currencyNames.get(position);
                // Save the selected currency in SharedPreferences
                currencyPreferenceManager.setSelectedCurrency(selectedCurrencyCode);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optionally handle the case where no item is selected
            }
        });
    }


    private void applyTheme(boolean isDarkMode) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    private void savePreferences() {
        preferenceManager.setPortraitLocked(switchLockPortrait.isChecked());
        preferenceManager.setNotificationsEnabled(switchNotifications.isChecked());
    }
}

