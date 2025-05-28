package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.FollowRepository;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.FollowService;
import hoangnguyen.dev.personal_hub_backend.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String USER_STATUS_PREFIX = "user_status:";
    private static final String USER_STATUS_CHANNEL = "user_status_channel";

    @Override
    public void setUserOnline(Long userId) {
        redisTemplate.opsForValue().set(USER_STATUS_PREFIX + userId, "online");
        redisTemplate.convertAndSend(USER_STATUS_CHANNEL, userId);
    }

    @Override
    public void setUserOffline(Long userId) {
        redisTemplate.delete(USER_STATUS_PREFIX + userId);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        String key = USER_STATUS_PREFIX + userId;
        String status = (String) redisTemplate.opsForValue().get(key);
        return "online".equals(status);
    }

    @Override
    public List<UserResponse> getOnlineUsers(Long currentUserId) {
        // Fetch following (users the current user follows)
        List<Long> followingIds = followRepository.findAllByFollowerUserIDAndDeletedAtIsNull(currentUserId)
                .stream()
                .map(follow -> follow.getFollowing().getUserID())
                .toList();

        // Combine follower and following IDs, ensuring uniqueness
        List<Long> userIds = new ArrayList<>(followingIds);
        userIds = userIds.stream().distinct().toList();

        // Filter online users
        List<Long> onlineUserIds = userIds.stream()
                .filter(userId -> {
                    boolean isOnline = isUserOnline(userId);
                    if(!isOnline) return false;
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));
                    return user.getShowOnlineStatus();
                })
                .collect(Collectors.toList());

        if (onlineUserIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch user details from the database
        List<User> users = userRepository.findAllById(onlineUserIds);
        if (users.isEmpty()) {
            throw new ApiException(ErrorCodeEnum.USER_NOT_FOUND);
        }

        // Map to UserResponse
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
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
