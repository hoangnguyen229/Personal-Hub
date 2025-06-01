package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.response.MessageResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;

import java.util.List;

public interface MessageService {
    MessageResponse sendMessage(Long senderId, Long receiverId, String content);
    List<MessageResponse> getMessagesBetweenUsers(Long senderId, Long receiverId);
    List<MessageResponse> getPendingMessagesBetweenUsers(Long senderId, Long receiverId);
//    List<MessageResponse> getPendingMessages(Long userId);
    List<UserResponse> getConversationUsers(Long userId);
    List<UserResponse> getPendingConversationUsers(Long userId);
    void deliverOfflineMessages(Long userId);
    void acceptPendingConversation(Long senderId, Long receiverId);
    void rejectPendingConversation(Long senderId, Long receiverId);
}
