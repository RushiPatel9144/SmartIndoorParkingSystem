package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.handleLocation;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ca.tech.sense.it.smart.indoor.parking.system.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final LocationAdapter adapter;

    public SwipeToDeleteCallback(LocationAdapter adapter) {
        super(0, ItemTouchHelper.LEFT); // Enable left swipe
        this.adapter = adapter;
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
        int position = viewHolder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            adapter.removeItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;

        // Define button-like dimensions and appearance
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark)); // Background color

        // Define text properties
        Paint textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);

        // Draw red background on swipe
        RectF background = new RectF(
                itemView.getRight() + dX, // Left position
                itemView.getTop(),               // Top position
                itemView.getRight(),             // Right position
                itemView.getBottom()             // Bottom position
        );
        c.drawRect(background, paint);

        // Draw the button-like text
        String buttonText = String.valueOf(R.string.delete);
        float textWidth = textPaint.measureText(buttonText);
        float textHeight = textPaint.descent() - textPaint.ascent();
        float textX = itemView.getRight() - textWidth - 50; // Adjust padding as needed
        float textY = itemView.getTop() + (itemView.getHeight() + textHeight) / 2 - textPaint.descent();

        c.drawText(buttonText, textX, textY, textPaint);

        // Call super to handle item movement
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

}
