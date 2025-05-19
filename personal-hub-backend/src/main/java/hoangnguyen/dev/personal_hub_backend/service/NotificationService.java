package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.response.NotificationResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Notification;
import hoangnguyen.dev.personal_hub_backend.enums.NotificationTypeEnum;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void sendNotification(String username, String email, Long postId, NotificationTypeEnum type);
    NotificationResponse markAsRead(Long userId, Long notificationId);
    List<NotificationResponse> getNotificationsByUserId(Long userId);
    NotificationResponse deleteNotification(Long userId, Long notificationId);
    void deleteAllNotifications(Long userId);
}
