package ca.tech.sense.it.smart.indoor.parking.system.userUi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.tech.sense.it.smart.indoor.parking.system.manager.bookingManager.BookingManager;

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

        for (String slot : objects) {
            // Sanitize the slot ID before using it
            String sanitizedSlot = sanitizeSlotId(slot);

            bookingManager.getSlotService().checkSlotAvailability(locationId, sanitizedSlot, selectedDate, selectedHour, status -> {
                slotStatusMap.put(sanitizedSlot, status); // Use sanitized slot ID
                Log.d("SlotAdapter", "Slot: " + sanitizedSlot + ", Status: " + status); // Add logging
                notifyDataSetChanged(); // Refresh the adapter when status is updated
            }, error -> {
                slotStatusMap.put(sanitizedSlot, "unknown"); // Use sanitized slot ID
                Log.e("SlotAdapter", "Error checking slot availability for slot: " + sanitizedSlot, error); // Add logging
                notifyDataSetChanged(); // Refresh the adapter on error
            });
        }
    }

    // Add the sanitizeSlotId method here
    private String sanitizeSlotId(String slotId) {
        return slotId.replaceAll("[.#$\\[\\]]", "_"); // Replace invalid characters with '_'
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
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            textView.setTextColor(Color.rgb(102, 153, 0));
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
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
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            textView.setTextColor(Color.rgb(102, 153, 0));
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return view;
    }
}
