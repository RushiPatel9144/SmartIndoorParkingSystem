package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ca.tech.sense.it.smart.indoor.parking.system.viewModel.RateUsViewModel;


public class RateUsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public RateUsViewModelFactory(Context context) {
        this.context = context;
    }

    /** @noinspection unchecked*/
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RateUsViewModel.class)) {
            return (T) new RateUsViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

