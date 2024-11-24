package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import ca.tech.sense.it.smart.indoor.parking.system.manager.parkingManager.ParkingLocationManager;
import ca.tech.sense.it.smart.indoor.parking.system.utility.DialogUtil;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final LocationAdapter adapter;
    private final String ownerId;
    private final ParkingLocationManager parkingManager;

    // Constructor
    public SwipeToDeleteCallback(LocationAdapter adapter, String ownerId, ParkingLocationManager parkingManager) {
        super(0, ItemTouchHelper.LEFT); // Enable left swipe
        this.adapter = adapter;
        this.ownerId = ownerId;
        this.parkingManager = parkingManager;
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
                    viewHolder.itemView.getContext(),
                    "Confirm Deletion",
                    "Are you sure you want to delete this location?",
                    15000,
                    1000,
                    () -> {
                        parkingManager.deleteParkingLocation(
                                viewHolder.itemView.getContext(),
                                ownerId,
                                locationId
                        );
                        adapter.removeItem(position);
                    },
                    adapter::notifyDataSetChanged
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
                itemView.getTop() + 32,
                itemView.getRight() ,
                itemView.getBottom() -32
        );
        c.drawRect(background, backgroundPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);

        String buttonText = "Delete";
        float textWidth = textPaint.measureText(buttonText);
        float textHeight = textPaint.descent() - textPaint.ascent();

        float textX = background.left + (background.width() - textWidth) / 2;
        float textY = background.top + (background.height() + textHeight) / 2 - textPaint.descent();

        c.drawText(buttonText, textX, textY, textPaint);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        // Reset the background when swipe is canceled
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
