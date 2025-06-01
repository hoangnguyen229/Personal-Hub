package hoangnguyen.dev.personal_hub_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.service.MessageService;
import hoangnguyen.dev.personal_hub_backend.service.UserStatusService;
import hoangnguyen.dev.personal_hub_backend.service.impl.UserStatusServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStatusListener {

    private final RedisMessageListenerContainer container;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void registerListener() {
        container.addMessageListener((message, pattern) -> {
            try {
                UserResponse userResponse = objectMapper.readValue(message.getBody(), UserResponse.class);
                Long userId = userResponse.getUserID();
                messageService.deliverOfflineMessages(userId);
                messagingTemplate.convertAndSend("/topic/user_status_channel", userResponse);
            } catch (Exception e) {
                System.err.println("❌ Error parsing user status message: " + e.getMessage());
            }
        }, new ChannelTopic("user_status_channel"));
    }
}
