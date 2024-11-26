package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionManager;

public class BaseNetworkFragment extends Fragment implements NetworkManager.NetworkListener {

    private int fragmentContainerId = R.id.flFragment;

    @Override
    public void onResume() {
        super.onResume();
        NetworkManager.getInstance().startMonitoring(requireContext());
        NetworkManager.getInstance().setNetworkListener(this);

        // Check network every time the fragment is resumed
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            openNoNetworkFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NetworkManager.getInstance().stopMonitoring(requireContext());
        NetworkManager.getInstance().setNetworkListener(null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            openNoNetworkFragment();
        } else {
            NetworkManager.getInstance().startMonitoring(requireContext());
        }
    }

    /**
     * Replace the current fragment with NoNetworkFragment in the specified container.
     * If no container ID is specified, it will default to the main fragment container (flFragment).
     */
    public void openNoNetworkFragment() {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        SessionManager sessionManager = new SessionManager(requireContext());
        String userType = sessionManager.getUserType();
        if (Objects.equals(userType, "owner")){
            fragmentContainerId = R.id.fragment_container_owner;
        }

        if (getActivity() != null && getActivity().getSupportFragmentManager().findFragmentByTag(NoNetworkFragment.class.getSimpleName()) == null) {
        transaction.replace(fragmentContainerId, new NoNetworkFragment()) // Dynamically use the container ID
                .addToBackStack(null)
                .commit();
        }
    }

    @Override
    public void onNetworkAvailable() {
        // network is back
    }

    @Override
    public void onNetworkLost() {
        requireActivity().runOnUiThread(this::openNoNetworkFragment);
    }
}
