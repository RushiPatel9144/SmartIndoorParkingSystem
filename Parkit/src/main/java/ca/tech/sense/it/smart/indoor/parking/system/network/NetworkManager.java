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
                    // If network was previously unavailable, show a message
                    if (wasNetworkLost) {
                        showToast(context,
                                context.getString(R.string.network_is_available));
                        wasNetworkLost = false; // Reset the flag
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    // Network lost
                    wasNetworkLost = true;
                    Intent intent = new Intent(context, NoNetworkActivity.class);
                    context.startActivity(intent);

                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {

                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);// Network has internet access
                }
            };

            cm.registerNetworkCallback(
                    new android.net.NetworkRequest.Builder().build(),
                    networkCallback
            );
        }
    }

    public void stopMonitoring(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);
        }
    }


    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}