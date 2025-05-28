package hoangnguyen.dev.personal_hub_backend.listener;

import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.MessageService;
import hoangnguyen.dev.personal_hub_backend.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserStatusService userStatusService;
    private final MessageService messageService;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetail) {
            Long userId = ((CustomUserDetail) auth.getPrincipal()).getId();
            userStatusService.setUserOnline(userId);
            messageService.deliverOfflineMessages(userId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetail) {
            Long userId = ((CustomUserDetail) auth.getPrincipal()).getId();
            userStatusService.setUserOffline(userId);
        }
    }
}
