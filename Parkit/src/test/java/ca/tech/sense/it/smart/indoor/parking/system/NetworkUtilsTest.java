package ca.tech.sense.it.smart.indoor.parking.system;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.tech.sense.it.smart.indoor.parking.system.network.NetworkUtils;

public class NetworkUtilsTest {

    @Mock
    private Context mockContext;

    @Mock
    private ConnectivityManager mockConnectivityManager;

    @Mock
    private Network mockNetwork;

    @Mock
    private NetworkCapabilities mockNetworkCapabilities;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
    }

    @Test
    public void testNetworkAvailable() {
        // Mocking the behavior for an available network
        when(mockConnectivityManager.getActiveNetwork()).thenReturn(mockNetwork);
        when(mockConnectivityManager.getNetworkCapabilities(mockNetwork)).thenReturn(mockNetworkCapabilities);
        when(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);

        // Call the method and verify the result
        boolean result = NetworkUtils.isNetworkAvailable(mockContext);
        assertTrue(result);  // Network should be available
    }

    @Test
    public void testNetworkNotAvailable() {
        // Mocking the behavior for no active network
        when(mockConnectivityManager.getActiveNetwork()).thenReturn(mockNetwork);
        when(mockConnectivityManager.getNetworkCapabilities(mockNetwork)).thenReturn(mockNetworkCapabilities);
        when(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false);

        // Call the method and verify the result
        boolean result = NetworkUtils.isNetworkAvailable(mockContext);
        assertFalse(result);  // Network should not be available
    }

    @Test
    public void testNoActiveNetwork() {
        // Mocking the behavior for no active network
        when(mockConnectivityManager.getActiveNetwork()).thenReturn(null);

        // Call the method and verify the result
        boolean result = NetworkUtils.isNetworkAvailable(mockContext);
        assertFalse(result);  // Network should not be available
    }
}
