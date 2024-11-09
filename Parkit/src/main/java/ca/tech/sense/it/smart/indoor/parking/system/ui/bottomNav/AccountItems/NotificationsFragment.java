/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.model.Notification;
import ca.tech.sense.it.smart.indoor.parking.system.ui.adapters.NotificationAdapter;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private Button btnClearNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private DatabaseReference notificationsRef;
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
        // Initialize notification list and adapter
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(notificationAdapter);
        // Setup ItemTouchHelper for swipe-to-delete functionality
        setupSwipeToDelete();
        // Get current user and reference to their notifications in Firebase
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        notificationsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notifications");
        // Fetch notifications from Firebase in real-time
        listenForNotifications();
        btnClearNotifications.setOnClickListener(v -> clearNotifications());

        return view;
    }
    private void listenForNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    String id = notificationSnapshot.getKey();
                    String title = notificationSnapshot.child("title").getValue(String.class);
                    String message = notificationSnapshot.child("message").getValue(String.class);
                    Long timestamp = notificationSnapshot.child("timestamp").getValue(Long.class);

                    if (timestamp != null) {
                        Notification notification = new Notification(id, title, message, timestamp);
                        notificationList.add(notification);
                    }
                }
                notificationList.sort(Collections.reverseOrder());
                btnClearNotifications.setVisibility(View.VISIBLE);
                notificationAdapter.notifyDataSetChanged();
                // Toggle empty state image based on the list size
                toggleEmptyState();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void clearNotifications() {
        // Create a list to hold notification IDs to delete
        List<String> notificationIdsToDelete = new ArrayList<>();
        for (Notification notification : notificationList) {
            notificationIdsToDelete.add(notification.getId());
        }
        // Delete each notification from Firebase
        for (String notificationId : notificationIdsToDelete) {
            deleteNotificationFromFirebase(notificationId);
        }
        // Clear the local list after deleting
        notificationList.clear();
        notificationAdapter.notifyDataSetChanged();
    }

    private void deleteNotificationFromFirebase(String notificationId) {
        notificationsRef.child(notificationId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    //success
                })
                .addOnFailureListener(e -> {
                    // error
                });
    }

    private void toggleEmptyState() {
        if (notificationList.isEmpty()) {
            ivEmptyState.setVisibility(View.VISIBLE);
            tvEmptyStateText.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            btnClearNotifications.setVisibility(View.GONE);
        } else {
            ivEmptyState.setVisibility(View.GONE);
            tvEmptyStateText.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            btnClearNotifications.setVisibility(View.VISIBLE);
        }
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
                    Notification notification = notificationList.get(position);
                    // Delete from Firebase
                    deleteNotificationFromFirebase(notification.getId());
                    // Remove from local list and notify adapter
                    notificationList.remove(position);
                    notificationAdapter.notifyItemRemoved(position);
                    // Toggle empty state if list becomes empty
                    toggleEmptyState();
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvNotifications);
    }
}
