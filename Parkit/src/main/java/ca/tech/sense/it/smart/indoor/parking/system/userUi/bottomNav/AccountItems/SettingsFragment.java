/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.manager.notificationManager.NotificationManagerHelper;
import ca.tech.sense.it.smart.indoor.parking.system.manager.preferenceManager.PreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.currency.Currency;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyManager;
import ca.tech.sense.it.smart.indoor.parking.system.currency.CurrencyPreferenceManager;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;
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
    private CardView currencyCardView;

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
        fetchUserData();
        return view;
    }

    private void initializeUIElements(View view) {
        switchLockPortrait = view.findViewById(R.id.switch_lock_portrait);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchTheme = view.findViewById(R.id.switch_theme);
        spinnerCurrency = view.findViewById(R.id.spinner_currency);
        currencyCardView = view.findViewById(R.id.currencyCardView);
    }

    public void fetchUserData() {
        SessionManager sessionManager = SessionManager.getInstance(getContext());
        String userType = sessionManager.getUserType();
        if (userType != null) {
            if (USER_TYPE_OWNER.equals(userType)) {
                currencyCardView.setVisibility(View.GONE);
            } else if (USER_TYPE_USER.equals(userType)) {
                currencyCardView.setVisibility(View.VISIBLE);
            }
        } else {
            // Handle the case where userType is null
            currencyCardView.setVisibility(View.GONE);
        }
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
        // Get the currency manager and list of available currencies
        CurrencyManager currencyManager = CurrencyManager.getInstance();
        Map<String, Currency> currencies = currencyManager.getCurrencies();

        // Extract the currency codes for display in the spinner
        List<String> currencyNames = new ArrayList<>(currencies.keySet());

        // Handle the case where no currencies are available
        if (currencyNames.isEmpty()) {
            showError(getString(R.string.no_currencies_available));
            return;
        }

        // Create and set the ArrayAdapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, currencyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        // Retrieve the stored currency from SharedPreferences
        CurrencyPreferenceManager currencyPreferenceManager = new CurrencyPreferenceManager(requireActivity());
        String storedCurrencyCode = currencyPreferenceManager.getSelectedCurrency();

        // If stored currency exists, set it in the spinner
        setStoredCurrencyInSpinner(currencyNames, storedCurrencyCode);

        // Handle item selection from the spinner
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Save selected currency to preferences
                saveSelectedCurrency(currencyNames.get(position), currencyPreferenceManager);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optionally handle case where no item is selected
            }
        });
    }

    /**
     * Sets the stored currency in the spinner, if it exists.
     */
    private void setStoredCurrencyInSpinner(List<String> currencyNames, String storedCurrencyCode) {
        int storedCurrencyPosition = currencyNames.indexOf(storedCurrencyCode);
        if (storedCurrencyPosition != -1) {
            spinnerCurrency.setSelection(storedCurrencyPosition);
        }
    }

    /**
     * Saves the selected currency in SharedPreferences.
     */
    private void saveSelectedCurrency(String selectedCurrencyCode, CurrencyPreferenceManager currencyPreferenceManager) {
        currencyPreferenceManager.setSelectedCurrency(selectedCurrencyCode);
    }

    /**
     * Shows an error message (e.g., no currencies available).
     */
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

