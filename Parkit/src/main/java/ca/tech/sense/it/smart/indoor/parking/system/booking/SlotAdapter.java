package ca.tech.sense.it.smart.indoor.parking.system.booking;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SlotAdapter extends ArrayAdapter<String> {
    private Map<String, String> slotStatusMap = new HashMap<>();
    private String locationId;
    private String selectedDate;
    private String selectedHour;
    private BookingManager bookingManager;

    public SlotAdapter(Context context, int resource, List<String> objects, String locationId, String selectedDate, String selectedHour, BookingManager bookingManager) {
        super(context, resource, objects);
        this.locationId = locationId;
        this.selectedDate = selectedDate;
        this.selectedHour = selectedHour;
        this.bookingManager = bookingManager;

        // Initialize slotStatusMap with the status of each slot
        for (String slot : objects) {
            bookingManager.checkSlotAvailability(locationId, slot, selectedDate, selectedHour, status -> {
                slotStatusMap.put(slot, status);
                Log.d("SlotAdapter", "Slot: " + slot + ", Status: " + status); // Add logging
                notifyDataSetChanged(); // Refresh the adapter when status is updated
            }, error -> {
                slotStatusMap.put(slot, "unknown");
                Log.e("SlotAdapter", "Error checking slot availability for slot: " + slot, error); // Add logging
                notifyDataSetChanged(); // Refresh the adapter on error
            });
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        String slot = getItem(position);
        String status = slotStatusMap.get(slot);
        Log.d("SlotAdapter", "getView - Slot: " + slot + ", Status: " + status); // Add logging
        if ("occupied".equals(status)) {
            textView.setTextColor(Color.GRAY);
        } else {
            textView.setTextColor(Color.BLACK);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        String slot = getItem(position);
        String status = slotStatusMap.get(slot);
        Log.d("SlotAdapter", "getDropDownView - Slot: " + slot + ", Status: " + status); // Add logging
        if ("occupied".equals(status)) {
            textView.setTextColor(Color.GRAY);
        } else {
            textView.setTextColor(Color.BLACK);
        }
        return view;
    }
}


