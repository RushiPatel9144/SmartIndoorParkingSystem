package ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager;

import static android.content.ContentValues.TAG;
import static ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.launcherUtililty.ToastHelper.showToast;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSensor;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;
import ca.tech.sense.it.smart.indoor.parking.system.utility.ParkingInterface;

public class ParkingSlotManager {
    private final DatabaseReference databaseReference;
    private final DatabaseReference ownerReference;
    private static final String PATH_SLOTS = "slots";

    public ParkingSlotManager() {
        databaseReference = FirebaseDatabaseSingleton.getInstance().getReference("parkingLocations");
        ownerReference = FirebaseDatabaseSingleton.getInstance().getReference("owners");
    }

    public void addSlotToLocation(String locationId, String ownerId, Context context, ParkingSlot slot, ParkingSensor sensor) {
        if (locationId == null || ownerId == null) {
            showToast(context, "Invalid location or owner ID");
            return;
        }

        DatabaseReference slotsRef = databaseReference.child(locationId).child(PATH_SLOTS);
        DatabaseReference ownerSlotsRef = ownerReference.child(ownerId).child("parkingLocationIds").child(locationId).child(PATH_SLOTS);

        String slotId = slotsRef.push().getKey();
        if (slotId == null) {
            showToast(context, context.getString(R.string.failed_to_generate_slot_id));
            return;
        }

        Task<Void> mainDbTask = slotsRef.child(slotId).setValue(slot);
        Task<Void> ownerDbTask = ownerSlotsRef.child(slotId).setValue(slot);

        Tasks.whenAll(mainDbTask, ownerDbTask)
                .addOnSuccessListener(aVoid -> addSensorToSlot(slotsRef.child(slotId), ownerSlotsRef.child(slotId), sensor, context))
                .addOnFailureListener(e -> {
                    showToast(context, context.getString(R.string.failed_to_save_slot_data));
                    Log.e(TAG, "Failed to save slot data: " + e.getMessage());
                });
    }

    private void addSensorToSlot(DatabaseReference slotRef, DatabaseReference ownerSlotRef, ParkingSensor sensor, Context context) {
        Task<Void> mainSensorTask = slotRef.child("sensor").setValue(sensor);
        Task<Void> ownerSensorTask = ownerSlotRef.child("sensor").setValue(sensor);

        Tasks.whenAll(mainSensorTask, ownerSensorTask)
                .addOnSuccessListener(aVoid -> showToast(context, context.getString(R.string.slot_added_successfully)))
                .addOnFailureListener(e -> {
                    showToast(context, context.getString(R.string.failed_to_save_sensor_data));
                    Log.e(TAG, "Failed to save sensor data: " + e.getMessage());
                });
    }


    // Fetch slots of a specific parking location
    public void fetchSlotsForLocation(String locationId, final ParkingInterface.FetchSlotsCallback callback) {
        databaseReference.child(locationId).child(PATH_SLOTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, ParkingSlot> slots = new HashMap<>();
                for (DataSnapshot slotSnapshot : dataSnapshot.getChildren()) {
                    ParkingSlot slot = slotSnapshot.getValue(ParkingSlot.class);
                    if (slot != null) {
                        slot.setId(slotSnapshot.getKey()); // Set the ID from Firebase
                        slots.put(slotSnapshot.getKey(), slot);
                    }
                }
                callback.onFetchSuccess(slots);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFetchFailure(databaseError.toException());
            }
        });
    }

}
