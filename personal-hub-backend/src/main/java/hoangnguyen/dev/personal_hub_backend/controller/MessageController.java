package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.MessageRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.MessageResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestBody MessageRequest messageRequest,
            @AuthenticationPrincipal CustomUserDetail userDetail
            ){
        MessageResponse messageResponse = messageService.sendMessage(
                userDetail.getId(),
                messageRequest.getReceiverId(),
                messageRequest.getContent()
        );
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/{receiverId}")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long receiverId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        List<MessageResponse> messageResponse = messageService.getMessagesBetweenUsers(
                userDetail.getId(),
                receiverId
        );
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<UserResponse>> getConversations(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        List<UserResponse> conversationUsers = messageService.getConversationUsers(userDetail.getId());
        return ResponseEntity.ok(conversationUsers);
    }

    @GetMapping("/pending-conversations")
    public ResponseEntity<List<UserResponse>> getPendingConversations(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        List<UserResponse> pendingConversationUsers = messageService.getPendingConversationUsers(userDetail.getId());
        return ResponseEntity.ok(pendingConversationUsers);
    }

    @GetMapping("/pending/{senderId}")
    public ResponseEntity<List<MessageResponse>> getPendingMessagesBetweenUsers(
            @PathVariable Long senderId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        List<MessageResponse> pendingMessages = messageService.getPendingMessagesBetweenUsers(senderId, userDetail.getId());
        return ResponseEntity.ok(pendingMessages);
    }

    @PostMapping("/accept-conversation/{senderId}")
    public ResponseEntity<Void> acceptPendingConversation(
            @PathVariable Long senderId,
            @AuthenticationPrincipal CustomUserDetail userDetail) {
        messageService.acceptPendingConversation(senderId, userDetail.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject-conversation/{senderId}")
    public ResponseEntity<Void> rejectPendingConversation(
            @PathVariable Long senderId,
            @AuthenticationPrincipal CustomUserDetail userDetail) {
        messageService.rejectPendingConversation(senderId, userDetail.getId());
        return ResponseEntity.ok().build();
    }
}
