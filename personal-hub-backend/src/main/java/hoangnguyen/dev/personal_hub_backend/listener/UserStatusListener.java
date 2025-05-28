package hoangnguyen.dev.personal_hub_backend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoangnguyen.dev.personal_hub_backend.service.MessageService;
import hoangnguyen.dev.personal_hub_backend.service.UserStatusService;
import hoangnguyen.dev.personal_hub_backend.service.impl.UserStatusServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStatusListener {
    private final MessageService messageService;

    @EventListener
    public void onUserStatusChange(RedisMessageListenerContainer container) {
        container.addMessageListener((message, pattern) -> {
            Long userId = (Long) new ObjectMapper().convertValue(message.getBody(), Long.class);
            messageService.deliverOfflineMessages(userId);
        }, new ChannelTopic("user_status_channel"));
    }
}
