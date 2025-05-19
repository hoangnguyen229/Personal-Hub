package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.response.NotificationResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        List<NotificationResponse> notificationResponses = notificationService.getNotificationsByUserId(userDetail.getId());
        return ResponseEntity.ok(notificationResponses);
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        NotificationResponse notificationResponse = notificationService.markAsRead(userDetail.getId(), notificationId);
        return ResponseEntity.ok(notificationResponse);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        NotificationResponse notificationResponse = notificationService.deleteNotification(userDetail.getId(), notificationId);
        return ResponseEntity.ok(notificationResponse);
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        notificationService.deleteAllNotifications(userDetail.getId());
        return ResponseEntity.ok().build();
    };
}
