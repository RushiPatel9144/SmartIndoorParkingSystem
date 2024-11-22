package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.LoginViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.repository.AuthRepository;

public class LoginViewModelFactory implements ViewModelProvider.Factory  {
    private final AuthRepository authRepository;

    public LoginViewModelFactory(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @NonNull
    @Override
    public LoginViewModel create(@NonNull Class modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            // Directly return the LoginViewModel without casting
            return new LoginViewModel(authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

