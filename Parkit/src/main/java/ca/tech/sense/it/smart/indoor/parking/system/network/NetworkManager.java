package ca.tech.sense.it.smart.indoor.parking.system.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class NetworkManager {

    private static final String TAG = "NetworkManager";

    private static NetworkManager instance;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean wasInternetAvailable = true;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
                    Log.d(TAG, "Network is available");
                    checkInternetAccessAsync(hasInternet -> {
                        if (hasInternet && !wasInternetAvailable) {
                            handleNetworkAvailability(context);
                        }
                    });
                }

                @Override
                public void onLost(@NonNull Network network) {
                    Log.d(TAG, "Network is lost");
                    wasInternetAvailable = false;
                }
            };

            cm.registerNetworkCallback(new android.net.NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(), networkCallback);
        }
    }

    public void stopMonitoring(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }

    private void handleNetworkAvailability(Context context) {
        if (!wasInternetAvailable) { // Show toast only if the network was previously unavailable
            showToast(context, context.getString(R.string.network_is_available));
            wasInternetAvailable = true;
        }
    }

    private void checkInternetAccessAsync(InternetCheckCallback callback) {
        executor.execute(() -> {
            try {
                boolean isConnected = InetAddress.getByName("google.com").isReachable(3000);
                new Handler(Looper.getMainLooper()).post(() -> callback.onResult(isConnected));
            } catch (Exception e) {
                Log.e(TAG, "Error during connectivity check: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onResult(false));
            }
        });
    }

    private void showToast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public interface InternetCheckCallback {
        void onResult(boolean hasInternet);
    }
}
