package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;

import java.util.List;

public interface UserStatusService {
    void setUserOnline(Long userId);
    void setUserOffline(Long userId);
    boolean isUserOnline(Long userId);
    List<UserResponse> getOnlineUsers(Long currentUserId);
}
