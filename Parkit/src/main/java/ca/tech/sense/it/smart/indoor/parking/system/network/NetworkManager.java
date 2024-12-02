package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;
import androidx.annotation.NonNull;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import java.net.InetAddress;

public class NetworkManager {

    private static NetworkManager instance;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean wasNetworkLost = false;
    private NetworkListener networkListener;
    private String lastToastMessage = null; // To avoid repeated toast messages

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // Register a listener for network changes (for fragments or activities)
    public void setNetworkListener(NetworkListener listener) {
        this.networkListener = listener;
    }

    // Start monitoring the network state
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

    // Stop monitoring when not needed
    public void stopMonitoring(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);
        }
    }

    private void handleNetworkAvailability(Context context, Network network) {
        if (hasInternetAccess(context, network)) {
            if (wasNetworkLost) {
                showToastOnce(context, context.getString(R.string.network_is_available));
                wasNetworkLost = false;
            }
            notifyNetworkAvailable();
        } else {
            showToastOnce(context, context.getString(R.string.no_internet_connection));
            notifyNetworkLost();
        }
    }

    private void handleNetworkLoss(Context context) {
        if (!wasNetworkLost) {
            showToastOnce(context, context.getString(R.string.no_internet_connection));
            wasNetworkLost = true;
        }
        notifyNetworkLost();
    }

    private void handleNetworkCapabilitiesChange(Context context, NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            if (wasNetworkLost) {
                showToastOnce(context, context.getString(R.string.network_is_available));
                wasNetworkLost = false;
            }
            notifyNetworkAvailable();
        } else {
            showToastOnce(context, context.getString(R.string.no_internet_connection));
            notifyNetworkLost();
        }
    }

    private void notifyNetworkAvailable() {
        if (networkListener != null) {
            networkListener.onNetworkAvailable();
        }
    }

    private void notifyNetworkLost() {
        if (networkListener != null) {
            networkListener.onNetworkLost();
        }
    }

    private boolean hasInternetAccess(Context context, Network network) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            try {
                InetAddress address = InetAddress.getByName("google.com"); // Perform DNS lookup
                return address.isReachable(2000); //Check if the host is reachable within a timeout
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    // Ensure only one toast for the current state
    private void showToastOnce(Context context, String message) {
        if (!message.equals(lastToastMessage)) {
            showToast(context, message);
            lastToastMessage = message;
        }
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Define the listener interface for fragments or activities to implement
    public interface NetworkListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
}
