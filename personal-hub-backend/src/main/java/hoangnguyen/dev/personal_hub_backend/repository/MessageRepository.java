package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Message;
import hoangnguyen.dev.personal_hub_backend.enums.MessageApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationKeyInOrderBySentAtAsc(List<String> conversationKey);
    List<Message> findBySenderUserIDOrReceiverUserID (Long senderId, Long receiverId);
    List<Message> findByReceiverUserIDAndApprovalStatus(Long receiverId, MessageApprovalStatus approvalStatus);
    List<Message> findByConversationKeyInAndApprovalStatus(List<String> conversationKey, MessageApprovalStatus approvalStatus);
    List<Message> findByConversationKeyIn(List<String> conversationKey);
    List<Message> findBySenderUserIDOrReceiverUserIDAndApprovalStatus(Long senderId, Long receiverId, MessageApprovalStatus approvalStatus);
    List<Message> findByConversationKeyAndApprovalStatus(String conversationKey, MessageApprovalStatus approvalStatus);
    Optional<Object> findFirstBySenderUserIDAndReceiverUserIDAndApprovalStatus(Long senderId, Long receiverId, MessageApprovalStatus messageApprovalStatus);
    Optional<Object> findFirstByReceiverUserIDAndSenderUserIDAndApprovalStatus(Long receiverId, Long senderId, MessageApprovalStatus messageApprovalStatus);
}
