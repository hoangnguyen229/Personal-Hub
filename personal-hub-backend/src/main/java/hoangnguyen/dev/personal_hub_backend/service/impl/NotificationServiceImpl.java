package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.config.RabbitMQConfig;
import hoangnguyen.dev.personal_hub_backend.config.WebSocketConfig;
import hoangnguyen.dev.personal_hub_backend.dto.response.NotificationResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Notification;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.NotificationTypeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.NotificationRepository;
import hoangnguyen.dev.personal_hub_backend.repository.PostRepository;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate webSocketTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final WebSocketConfig webSocketConfig;

    @Override
    @Transactional
    public void sendNotification(String username, String email, Long postId, NotificationTypeEnum type){
        Map<String, Object> notification = new HashMap<>();
        notification.put("username", username);
        notification.put("email", email);
        notification.put("postId", postId);
        notification.put("type", type.getValue());
        notification.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getPersonalHubExchange(),
                rabbitMQConfig.getNotificationRoutingKey(),
                notification
        );
    }

    @Transactional
    @RabbitListener(queues = "${rabbitmq.queue.notification.name}")
    public void processNotification(Map<String, Object> notification) {
        String username = (String) notification.get("username");
        String email = (String) notification.get("email");

        Object postIdObj = notification.get("postId");
        Long postId = postIdObj != null ? ((Number) postIdObj).longValue() : null;

        String typeStr = (String) notification.get("type");
        NotificationTypeEnum type = NotificationTypeEnum.fromValue(typeStr);


//        Object timestampObj = notification.get("timestamp");
//        Timestamp timestamp = new Timestamp((Long) timestampObj);

        User receiver = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        String content;
        Notification notify = new Notification();
        notify.setUser(receiver);

        switch (type) {
            case FOLLOW:
                content = username + " has followed you";
                break;
            case COMMENT:
                if (postId == null) throw new ApiException(ErrorCodeEnum.POST_NOT_FOUND);
                Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
                content = username + " commented on your post '" + post.getTitle() + "'";
                notify.setPost(post);
                break;
            case LIKE:
                if (postId == null) throw new ApiException(ErrorCodeEnum.POST_NOT_FOUND);
                post = postRepository.findById(postId)
                        .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
                content = username + " liked your post '" + post.getTitle() + "'";
                notify.setPost(post);
                break;
            case POST:
                if (postId == null) throw new ApiException(ErrorCodeEnum.POST_NOT_FOUND);
                post = postRepository.findById(postId)
                        .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
                content = username + " created a new post '" + post.getTitle() + "'";
                notify.setPost(post);
                break;
            default:
                content = "notification";
        }

        notify.setContent(content);
        Notification savedNotify = notificationRepository.save(notify);

        NotificationResponse notificationResponse = mapToNotificationResponse(savedNotify);

        String redisKey = "notification:unread:" + receiver.getUserID() + ":" + savedNotify.getNotificationID();
        redisTemplate.opsForValue().set(redisKey, notificationResponse, 30, TimeUnit.DAYS);

        webSocketTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/notifications",
                notificationResponse
        );
    }

    @Override
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.NOTIFICATION_NOT_FOUND));

        String redisKey = "notification:unread:" + userId + ":" + notificationId;
        redisTemplate.delete(redisKey);

        notification.setIsRead(true);
        Notification savedNotification = notificationRepository.save(notification);

        return mapToNotificationResponse(savedNotification);
    }

    @Override
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        List<Notification> notificationResponses = notificationRepository.findNotificationByUserAndDeletedAtIsNull(user);
        return notificationResponses
                .stream()
                .map(this::mapToNotificationResponse)
                .toList();
    }

    @Override
    public NotificationResponse deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getUserID().equals(userId)) {
            throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
        }

        notification.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        Notification savedNotify = notificationRepository.save(notification);

        String redisKey = "notification:unread:" + userId + ":" + notificationId;
        redisTemplate.delete(redisKey);

        return mapToNotificationResponse(savedNotify);
    }

    @Override
    public void deleteAllNotifications(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        List<Notification> notifications = notificationRepository.findNotificationByUserAndDeletedAtIsNull(user);

        for (Notification notification : notifications){
            if(!notification.getUser().getUserID().equals(userId)){
                throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
            }
            notification.setDeletedAt(new Timestamp(System.currentTimeMillis()));
            notificationRepository.save(notification);
        }
    }

    private NotificationResponse mapToNotificationResponse(Notification savedNotify) {
         return NotificationResponse.builder()
                .notificationID(savedNotify.getNotificationID())
                .content(savedNotify.getContent())
                .isRead(savedNotify.getIsRead())
                .createdAt(savedNotify.getCreatedAt())
                .deletedAt(savedNotify.getDeletedAt())
                 .user(mapToUserResponse(savedNotify.getUser()))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userID(user.getUserID())
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .profilePic(user.getProfilePic())
                .authType(user.getAuthType().getValue())
                .build();
    }
}
