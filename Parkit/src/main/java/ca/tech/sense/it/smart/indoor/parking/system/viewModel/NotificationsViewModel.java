package ca.tech.sense.it.smart.indoor.parking.system.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import ca.tech.sense.it.smart.indoor.parking.system.model.Notification;
import ca.tech.sense.it.smart.indoor.parking.system.repository.NotificationRepository;

public class NotificationsViewModel extends ViewModel {
    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    private final NotificationRepository notificationRepository;
    public NotificationsViewModel() {
        notificationRepository = new NotificationRepository();
        loadNotifications();
    }
    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }
    private void loadNotifications() {
        notificationRepository.fetchNotifications(notificationsLiveData::setValue);
    }
    public void clearNotifications() {
        notificationRepository.clearAllNotifications();
    }
    public void deleteNotification(String id) {
        notificationRepository.deleteNotification(id);
    }
}
