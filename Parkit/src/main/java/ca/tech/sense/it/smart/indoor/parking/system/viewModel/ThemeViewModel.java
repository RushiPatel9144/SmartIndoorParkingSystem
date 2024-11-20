package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ThemeViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isDarkTheme = new MutableLiveData<>();

    public LiveData<Boolean> getIsDarkTheme() {
        return isDarkTheme;
    }

    public void setIsDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme.setValue(isDarkTheme);
    }
}
