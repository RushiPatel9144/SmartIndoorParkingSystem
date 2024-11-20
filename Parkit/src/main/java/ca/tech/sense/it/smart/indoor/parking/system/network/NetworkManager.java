package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;
import androidx.annotation.NonNull;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class NetworkManager {

    private static NetworkManager instance;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean wasNetworkLost = false;

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void startMonitoring(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    handleNetworkAvailability(context, network);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    handleNetworkLoss(context);
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    handleNetworkCapabilitiesChange(context, networkCapabilities);
                }
            };

            cm.registerNetworkCallback(new android.net.NetworkRequest.Builder().build(), networkCallback);
        }
    }

    public void stopMonitoring(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);
        }
    }

    public void handleNetworkAvailability(Context context, Network network) {
        if (hasInternetAccess(context, network)) {
            if (wasNetworkLost) {
                showToast(context, context.getString(R.string.network_is_available));
                wasNetworkLost = false;
            }
        } else {
            showToastAndOpenNoNetworkActivity(context);
        }
    }

    public void handleNetworkLoss(Context context) {
        wasNetworkLost = true;
        showToastAndOpenNoNetworkActivity(context);
    }

    public void handleNetworkCapabilitiesChange(Context context, NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            if (wasNetworkLost) {
                showToast(context, context.getString(R.string.network_is_available));
                wasNetworkLost = false;
            }
        } else {
            showToastAndOpenNoNetworkActivity(context);
        }
    }

    private void showToastAndOpenNoNetworkActivity(Context context) {
        showToast(context, context.getString(R.string.no_internet_connection));
        Intent intent = new Intent(context, NoNetworkActivity.class);
        context.startActivity(intent);
    }

    private boolean hasInternetAccess(Context context, Network network) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
