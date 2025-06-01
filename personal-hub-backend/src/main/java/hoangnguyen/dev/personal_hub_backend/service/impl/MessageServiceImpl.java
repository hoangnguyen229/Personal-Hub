package hoangnguyen.dev.personal_hub_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoangnguyen.dev.personal_hub_backend.config.RabbitMQConfig;
import hoangnguyen.dev.personal_hub_backend.dto.response.MessageResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.OfflineMessageResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Message;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.MessageApprovalStatus;
import hoangnguyen.dev.personal_hub_backend.enums.MessageStatusEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.FollowRepository;
import hoangnguyen.dev.personal_hub_backend.repository.MessageRepository;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.MessageService;
import hoangnguyen.dev.personal_hub_backend.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final UserStatusService userStatusService;
    private final SimpMessagingTemplate webSocketTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final ObjectMapper objectMapper;
    private final FollowRepository followRepository;

    @Override
    public MessageResponse sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        String conversationKey = senderId + "_" + receiverId;

        boolean hasPreviouslyApproved = messageRepository.findFirstBySenderUserIDAndReceiverUserIDAndApprovalStatus(senderId, receiverId, MessageApprovalStatus.APPROVED).isPresent()
                || messageRepository.findFirstBySenderUserIDAndReceiverUserIDAndApprovalStatus(receiverId, senderId, MessageApprovalStatus.APPROVED).isPresent();

        MessageApprovalStatus approvalStatus;
        if (hasPreviouslyApproved) {
            approvalStatus = MessageApprovalStatus.APPROVED;
        } else {
            // Nếu chưa từng phê duyệt, kiểm tra trạng thái follow
            boolean areFollowingEachOtherOrOneWayFollow =
                    followRepository.findFollowsByFollowerUserIDAndFollowingUserID(
                            sender.getUserID(), receiver.getUserID()
                    ).isPresent()
                            ||
                            followRepository.findFollowsByFollowerUserIDAndFollowingUserID(
                                    receiver.getUserID(), sender.getUserID()
                            ).isPresent();

            approvalStatus = areFollowingEachOtherOrOneWayFollow
                    ? MessageApprovalStatus.APPROVED
                    : MessageApprovalStatus.PENDING;
        }

        Message message = Message.builder()
                .conversationKey(conversationKey)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .status(MessageStatusEnum.SENT)
                .approvalStatus(approvalStatus)
                .build();

        messageRepository.save(message);

        MessageResponse messageResponse = mapToMessageResponse(message);

        boolean receiverOnline = userStatusService.isUserOnline(receiverId);

        if (approvalStatus == MessageApprovalStatus.APPROVED && receiverOnline) {
            webSocketTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/messages",
                    messageResponse
            );
            message.setStatus(MessageStatusEnum.DELIVERED);
            messageRepository.save(message);
        } else if (approvalStatus == MessageApprovalStatus.PENDING && receiverOnline) {
            webSocketTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/messages",
                    messageResponse
            );
            message.setStatus(MessageStatusEnum.DELIVERED);
            messageRepository.save(message);
        } else {
            OfflineMessageResponse offlineMessageResponse = OfflineMessageResponse.builder()
                    .messageId(message.getMessageID())
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .content(message.getContent())
                    .sentAt(message.getSentAt())
                    .status(String.valueOf(message.getStatus()))
                    .approvalStatus(message.getApprovalStatus().name())
                    .build();

            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getPersonalHubExchange(),
                    rabbitMQConfig.getMessageRoutingKey(),
                    offlineMessageResponse
            );

            String offlineKey = "offline:msg" + receiverId + ":" + message.getMessageID();
            redisTemplate.opsForValue().set(offlineKey, offlineMessageResponse);
        }
        return messageResponse;
    }

    @Override
    public void deliverOfflineMessages(Long receiverId) {
        User user = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        String pattern = "offline:msg" + receiverId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (!keys.isEmpty()) {
            for (String key : keys) {
                Object rawPayload = redisTemplate.opsForValue().get(key);

                if (rawPayload != null) {
                    OfflineMessageResponse payload = objectMapper.convertValue(rawPayload, OfflineMessageResponse.class);

                    Message message = messageRepository.findById(payload.getMessageId())
                            .orElseThrow(() -> new ApiException(ErrorCodeEnum.MESSAGE_NOT_FOUND));

                    if(message.getApprovalStatus() == MessageApprovalStatus.APPROVED){
                        webSocketTemplate.convertAndSendToUser(
                                user.getEmail(),
                                "/queue/messages",
                                payload
                        );

                        message.setStatus(MessageStatusEnum.DELIVERED);
                        messageRepository.save(message);
                    } else if (message.getApprovalStatus() == MessageApprovalStatus.PENDING) {
                        webSocketTemplate.convertAndSendToUser(
                                user.getEmail(),
                                "/queue/messages",
                                payload
                        );
                    }
                    redisTemplate.delete(key);
                }
            }
        }
    }

    @Override
    public List<MessageResponse> getMessagesBetweenUsers(Long senderId, Long receiverId) {
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        String conversationKey1 = sender.getUserID() + "_" + receiver.getUserID();
        String conversationKey2 = receiver.getUserID() + "_" + sender.getUserID();

//        List<Message> messages = messageRepository.findByConversationKeyInAndApprovalStatus(
//                List.of(conversationKey1, conversationKey2),
//                MessageApprovalStatus.APPROVED
//        );

        List<Message> messages = messageRepository.findByConversationKeyInOrderBySentAtAsc(
                List.of(conversationKey1, conversationKey2)
        );

        return messages.stream()
                .map(this::mapToMessageResponse)
                .toList();
    }

    @Override
    public List<MessageResponse> getPendingMessagesBetweenUsers(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        String conversationKey1 = sender.getUserID() + "_" + receiver.getUserID();
        String conversationKey2 = receiver.getUserID() + "_" + sender.getUserID();

        List<Message> messages = messageRepository.findByConversationKeyInAndApprovalStatus(
                List.of(conversationKey1, conversationKey2),
                MessageApprovalStatus.PENDING
        );

        return messages.stream()
                .map(this::mapToMessageResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getConversationUsers(Long userId) {
        List<Message> messages = messageRepository.findBySenderUserIDOrReceiverUserIDAndApprovalStatus(
                userId,
                userId,
                MessageApprovalStatus.APPROVED
        );

        Set<Long> userIds = new HashSet<>();
        for (Message message : messages) {
            if (!message.getSender().getUserID().equals(userId)) {
                userIds.add(message.getSender().getUserID());
            }
            if (!message.getReceiver().getUserID().equals(userId)) {
                userIds.add(message.getReceiver().getUserID());
            }
        }

        return userRepository.findAllById(userIds).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getPendingConversationUsers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        List<Message> pendingMessages = messageRepository.findByReceiverUserIDAndApprovalStatus(
                userId,
                MessageApprovalStatus.PENDING
        );

        Set<Long> userIds = new HashSet<>();
        for (Message message : pendingMessages) {
            userIds.add(message.getSender().getUserID());
        }

        return userRepository.findAllById(userIds).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void acceptPendingConversation(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        String conversationKey = sender.getUserID() + "_" + receiver.getUserID();
        List<Message> pendingMessages = messageRepository.findByConversationKeyAndApprovalStatus(
                conversationKey,
                MessageApprovalStatus.PENDING
        );

        if (pendingMessages.isEmpty()) {
            throw new ApiException(ErrorCodeEnum.NO_PENDING_MESSAGES);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        for (Message message : pendingMessages) {
            if (!message.getReceiver().getUserID().equals(receiverId)) {
                throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
            }
            message.setApprovalStatus(MessageApprovalStatus.APPROVED);
            message.setApprovedAt(now);
            message.setStatus(MessageStatusEnum.DELIVERED);
            messageRepository.save(message);

            // Gửi thông báo qua WebSocket tới người gửi nếu họ online
            boolean senderOnline = userStatusService.isUserOnline(senderId);
            if (senderOnline) {
                webSocketTemplate.convertAndSendToUser(
                        sender.getEmail(),
                        "/queue/messages",
                        mapToMessageResponse(message)
                );
            }
        }
    }

    @Override
    public void rejectPendingConversation(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        String conversationKey = sender.getUserID() + "_" + receiver.getUserID();
        List<Message> pendingMessages = messageRepository.findByConversationKeyAndApprovalStatus(
                conversationKey,
                MessageApprovalStatus.PENDING
        );

        if (pendingMessages.isEmpty()) {
            throw new ApiException(ErrorCodeEnum.NO_PENDING_MESSAGES);
        }

        for (Message message : pendingMessages) {
            if (!message.getReceiver().getUserID().equals(receiverId)) {
                throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
            }
            message.setApprovalStatus(MessageApprovalStatus.REJECTED);
            messageRepository.save(message);
        }
    }

//    @Override
    public List<MessageResponse> getPendingMessages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        List<Message> pendingMessages = messageRepository.findByReceiverUserIDAndApprovalStatus(
                user.getUserID(),
                MessageApprovalStatus.PENDING
        );

        return pendingMessages.stream()
                .map(this::mapToMessageResponse)
                .toList();
    }


    private MessageResponse mapToMessageResponse(Message message){
        return MessageResponse.builder()
                .messageID(message.getMessageID())
                .sender(mapToUserResponse(message.getSender()))
                .receiver(mapToUserResponse(message.getReceiver()))
                .content(message.getContent())
                .status(String.valueOf(message.getStatus()))
                .approvalStatus(String.valueOf(message.getApprovalStatus()))
                .approvedAt(message.getApprovedAt())
                .sentAt(message.getSentAt())
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
                .showOnlineStatus(user.getShowOnlineStatus())
                .build();
    }
}
