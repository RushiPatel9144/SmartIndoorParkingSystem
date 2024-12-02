package ca.tech.sense.it.smart.indoor.parking.system.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseDatabaseSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager.SessionDataManager;
import ca.tech.sense.it.smart.indoor.parking.system.model.Notification;

public class NotificationRepository {

    private final DatabaseReference notificationsRef;
    private String collection = "users";

    public NotificationRepository() {
        getUserType();
        FirebaseAuth firebaseAuth = FirebaseAuthSingleton.getInstance();
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabaseSingleton.getInstance();
        notificationsRef = firebaseDatabase.getReference(collection).child(userId).child("notifications");
    }

    public void getUserType(){
        SessionDataManager sessionDataManager = new SessionDataManager();
        String userType = sessionDataManager.getUserType();
        if (Objects.equals(userType, "owner")){
            collection = "owners";
        }
    }

    // Fetch notifications from Firebase, either from the network or cache
    public void fetchNotifications(Consumer<List<Notification>> callback) {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Notification> notifications = parseNotifications(snapshot);
                callback.accept(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error, possibly call callback with an empty list or error message
            }
        });
    }

    // Helper method to parse notifications from the DataSnapshot
    private List<Notification> parseNotifications(DataSnapshot snapshot) {
        List<Notification> notifications = new ArrayList<>();
        for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
            String id = notificationSnapshot.getKey();
            String title = notificationSnapshot.child("title").getValue(String.class);
            String message = notificationSnapshot.child("message").getValue(String.class);
            Long timestamp = notificationSnapshot.child("timestamp").getValue(Long.class);

            if (timestamp != null) {
                Notification notification = new Notification(id, title, message, timestamp);
                notifications.add(notification);
            }
        }
        return notifications;
    }

    // Delete a specific notification
    public void deleteNotification(String notificationId) {
        notificationsRef.child(notificationId).removeValue();
    }

    // Clear all notifications
    public void clearAllNotifications() {
        notificationsRef.removeValue();
    }
}
