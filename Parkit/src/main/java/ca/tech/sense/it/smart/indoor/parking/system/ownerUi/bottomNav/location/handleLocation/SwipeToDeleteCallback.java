package ca.tech.sense.it.smart.indoor.parking.system.ownerUi.bottomNav.location.handleLocation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final LocationAdapter adapter;
    private final String ownerId;
    private final ParkingLocationManager parkingManager;
    private final Fragment fragment;

    // Constructor
    public SwipeToDeleteCallback(Fragment fragment, LocationAdapter adapter, String ownerId, ParkingLocationManager parkingManager) {
        super(0, ItemTouchHelper.LEFT); // Enable left swipe
        this.adapter = adapter;
        this.ownerId = ownerId;
        this.parkingManager = parkingManager;
        this.fragment = fragment;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        // Do not allow move actions
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        String locationId;
        int position = viewHolder.getBindingAdapterPosition();

        if (position != RecyclerView.NO_POSITION) {
            LocationAdapter.LocationViewHolder locationViewHolder = (LocationAdapter.LocationViewHolder) viewHolder;
            locationId = locationViewHolder.getLocationId();

            DialogUtil.showTimedConfirmationDialog(
                    fragment,
                    fragment.requireContext().getString(R.string.confirm_deletion),
                    fragment.requireContext().getString(R.string.are_you_sure_you_want_to_delete_this_location),
                    15000,  // 15 seconds
                    1000,   // Update every 1 second
                    () -> {
                        // Perform the deletion action
                        parkingManager.deleteParkingLocation(
                                viewHolder.itemView.getContext(),
                                ownerId,
                                locationId
                        );
                        // Remove item from the adapter
                        adapter.removeItem(position);
                    },
                    () ->
                        // If cancelled, refresh the specific item
                        adapter.notifyItemChanged(position)
            );
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;

        float maxSwipeDistance = (float) (itemView.getWidth() / 3.5);
        if (dX < -maxSwipeDistance) {
            dX = -maxSwipeDistance;
        }

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#9E2424"));


        RectF background = new RectF(
                itemView.getRight()+ dX,
                itemView.getTop() + 32f,
                itemView.getRight() ,
                itemView.getBottom() - 32f
        );
        c.drawRect(background, backgroundPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);

        String buttonText = fragment.getString(R.string.delete);
        float textWidth = textPaint.measureText(buttonText);
        float textHeight = textPaint.descent() - textPaint.ascent();

        float textX = background.left + (background.width() - textWidth) / 2;
        float textY = background.top + (background.height() + textHeight) / 2 - textPaint.descent();

        c.drawText(buttonText, textX, textY, textPaint);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

}
