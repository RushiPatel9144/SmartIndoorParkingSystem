package ca.tech.sense.it.smart.indoor.parking.system.gate;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothClient {
    private static final String SERVER_MAC_ADDRESS = "E4:5F:01:C3:80:96"; // Raspberry Pi Bluetooth MAC
    private static final UUID UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;
    private BluetoothSocket socket = null;
    private BluetoothSocket tmp = null;
    private OutputStream outputStream;
    private InputStream inputStream;

    public BluetoothClient(Context context) {
        this.context = context;
    }

    public boolean connect() {
        try {
            // Check Bluetooth permissions
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false; // Permission not granted
            }

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(SERVER_MAC_ADDRESS);



            // Establish a Bluetooth connection
            socket = device.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT);
            bluetoothAdapter.cancelDiscovery();
            socket.connect();

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String message) {
        try {
            outputStream.write(message.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() {
        try {
            byte[] buffer = new byte[1024];
            int bytes = inputStream.read(buffer);
            return new String(buffer, 0, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
