package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.viewModel.NotificationsViewModel;
import ca.tech.sense.it.smart.indoor.parking.system.model.Notification;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.NotificationAdapter;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private RecyclerView rvNotifications;
    private Button btnClearNotifications;
    private NotificationAdapter notificationAdapter;
    private ImageView ivEmptyState;
    private TextView tvEmptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        btnClearNotifications = view.findViewById(R.id.btnClearNotifications);
        ivEmptyState = view.findViewById(R.id.ivEmptyState);
        tvEmptyStateText = view.findViewById(R.id.tvEmptyStateText);

        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupClearNotificationsButton();
        setupSwipeToDelete();

        return view;
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void setupObservers() {
        notificationsViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            notificationAdapter.setNotifications(notifications);
            toggleEmptyState(notifications.isEmpty());
        });
    }

    private void setupClearNotificationsButton() {
        btnClearNotifications.setOnClickListener(v -> notificationsViewModel.clearNotifications());
    }

    private void toggleEmptyState(boolean isEmpty) {
        ivEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        tvEmptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnClearNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Notification notification = notificationAdapter.getNotificationAt(position);
                    notificationsViewModel.deleteNotification(notification.getId());
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvNotifications);
    }
}
