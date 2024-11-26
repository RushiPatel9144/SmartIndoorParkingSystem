package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class NoNetworkFragment extends Fragment implements NetworkManager.NetworkListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_network, container, false);

        Button retryButton = view.findViewById(R.id.retryButton);
        retryButton.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                closeFragment();
            } else {
                Toast.makeText(requireContext(), getString(R.string.network_still_unavailable), Toast.LENGTH_SHORT).show();
            }
        });

        Button openNetworkSettingsButton = view.findViewById(R.id.openNetworkSettingsButton);
        openNetworkSettingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start monitoring network state when fragment is visible
        NetworkManager.getInstance().setNetworkListener(this);
        NetworkManager.getInstance().startMonitoring(requireContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop monitoring when fragment is no longer visible
        NetworkManager.getInstance().stopMonitoring(requireContext());
        NetworkManager.getInstance().setNetworkListener(null);
    }

    @Override
    public void onNetworkAvailable() {
        // Handle network available state
        closeFragment();
    }

    @Override
    public void onNetworkLost() {
        Log.d("NoNetworkFragment", "Network is lost again");
    }

    private void closeFragment() {
        if (isAdded() && isResumed()) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
