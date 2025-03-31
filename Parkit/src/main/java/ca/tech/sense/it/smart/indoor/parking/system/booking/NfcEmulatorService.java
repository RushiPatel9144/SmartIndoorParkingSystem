package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import android.nfc.cardemulation.HostApduService;

import java.io.UnsupportedEncodingException;

public class NfcEmulatorService extends HostApduService {

    private static final String TAG = "NfcEmulatorService";
    private String NFC_TAG;
    private String bookingId; // Variable to store booking ID

    @Override
    public void onCreate() {
        super.onCreate();
        // Log that the service has been created
        Log.d(TAG, "NFC Emulator Service Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Retrieve bookingId passed from Activity
        bookingId = intent.getStringExtra("bookingId"); // Now `bookingId` is retrieved from the Intent
        if (bookingId != null) {
            retrieveNFCTagForBooking(bookingId);
        } else {
            Log.e(TAG, "No bookingId provided in Intent");
        }

        return START_NOT_STICKY;
    }

    private void retrieveNFCTagForBooking(String bookingId) {
        // Reference to the specific booking in Firebase
        DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuthSingleton.getInstance().getCurrentUser().getUid())
                .child("bookings")
                .child(bookingId); // Use the passed bookingId

        // Retrieve the NFC Tag for the specified booking
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NFC_TAG = dataSnapshot.child("nfcTag").getValue(String.class);
                if (NFC_TAG != null) {

                    Log.d(TAG, "NFC Tag retrieved: " + NFC_TAG);
                } else {
                    Log.d(TAG, "No NFC tag found for the specified booking.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to retrieve NFC tag: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.d(TAG, "Processing APDU command...");

        // If the SELECT command is received, respond with the NFC tag
        if (commandApdu != null && isSelectApdu(commandApdu)) {
            Log.d(TAG, "SELECT command received.");

            // If NFC_TAG is not null, convert it to a byte array and respond with it
            if (NFC_TAG != null) {
                try {
                    byte[] nfcTagBytes = NFC_TAG.getBytes("UTF-8");  // Convert the NFC tag to a byte array using UTF-8 encoding
                    Log.d(TAG, "Responding with NFC Tag: " + NFC_TAG);
                    return nfcTagBytes;
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Error encoding NFC tag to byte array", e);
                    return new byte[]{(byte) 0x6F, (byte) 0x00}; // Default error response
                }
            } else {
                Log.d(TAG, "No NFC tag found for the specified booking.");
                return new byte[]{(byte) 0x6F, (byte) 0x00}; // Default error response if NFC_TAG is null
            }
        }

        // Default response for unknown commands
        return new byte[]{(byte) 0x6F, (byte) 0x00};  // Generic error response for unknown commands
    }


    private boolean isSelectApdu(byte[] commandApdu) {
        // Check if the APDU command is a SELECT command
        return commandApdu[0] == (byte) 0x00 && commandApdu[1] == (byte) 0xA4;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "NFC deactivated, reason: " + reason);
    }
}
