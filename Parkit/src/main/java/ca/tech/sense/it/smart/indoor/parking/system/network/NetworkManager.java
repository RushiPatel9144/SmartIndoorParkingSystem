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
    private boolean wasInternetAvailable = false;
    private NetworkListener networkListener;
    private String lastToastMessage = null;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void setNetworkListener(NetworkListener listener) {
        this.networkListener = listener;
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
                        } else if (!hasInternet && wasInternetAvailable) {
                            handleNetworkLoss(context);
                        }
                    });
                }

                @Override
                public void onLost(@NonNull Network network) {
                    Log.d(TAG, "Network is lost");
                    handleNetworkLoss(context);
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        Log.d(TAG, "Network capabilities changed: Internet capability available");
                        checkInternetAccessAsync(hasInternet -> {
                            if (hasInternet && !wasInternetAvailable) {
                                handleNetworkAvailability(context);
                            }
                        });
                    } else {
                        Log.d(TAG, "Network capabilities changed: Internet capability lost");
                        handleNetworkLoss(context);
                    }
                }
            };

            cm.registerNetworkCallback(new android.net.NetworkRequest.Builder().build(), networkCallback);
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
            wasInternetAvailable = true;
            showToastOnce(context, context.getString(R.string.network_is_available));
            notifyNetworkAvailable();
        }
    }

    private void handleNetworkLoss(Context context) {
        wasInternetAvailable = false;
        notifyNetworkLost();
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

    private void showToastOnce(Context context, String message) {
        if (!message.equals(lastToastMessage)) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
            lastToastMessage = message;
        }
    }

    public interface InternetCheckCallback {
        void onResult(boolean hasInternet);
    }

    public interface NetworkListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
}
