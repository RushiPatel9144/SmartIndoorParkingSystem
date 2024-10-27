// NotificationFragment.java
package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Notification;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.NotificationAdapter;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private Button btnClearNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        rvNotifications = view.findViewById(R.id.rvNotifications);
        btnClearNotifications = view.findViewById(R.id.btnClearNotifications);

        // Sample data for notifications
        notificationList = new ArrayList<>();
        notificationList.add(new Notification("Title 1", "This is the first notification.",  System.currentTimeMillis()));
        notificationList.add(new Notification("Title 2", "This is the second notification.",  System.currentTimeMillis()));


        notificationAdapter = new NotificationAdapter(notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(notificationAdapter);

        // Add the divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvNotifications.getContext(), DividerItemDecoration.VERTICAL);
        rvNotifications.addItemDecoration(dividerItemDecoration);

        btnClearNotifications.setOnClickListener(v -> {
            notificationList.clear();
            notificationAdapter.notifyDataSetChanged();
        });

        return view;
    }
}
