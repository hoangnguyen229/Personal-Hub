package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.dto.request.FollowRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.FollowResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Follow;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.NotificationTypeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.FollowRepository;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.FollowService;
import hoangnguyen.dev.personal_hub_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public FollowResponse followUser(FollowRequest request, Long currentUserId) {
        if(currentUserId.equals(request.getFollowingID())) throw new ApiException(ErrorCodeEnum.CANNOT_FOLLOW_YOURSELF);

        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        User following = userRepository.findById(request.getFollowingID())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        Optional<Follow> existingFollow = followRepository.findFollowsByFollowerUserIDAndFollowingUserID(
                follower.getUserID(), following.getUserID());

        Follow follow;
        if (existingFollow.isPresent()) {
            follow = existingFollow.get();
            if (follow.getDeletedAt() == null) {
                throw new ApiException(ErrorCodeEnum.ALREADY_FOLLOWED);
            }
            follow.setDeletedAt(null);
            follow.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        } else {
            follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
        }

        Follow savedFollow = followRepository.save(follow);

        notificationService.sendNotification(
                follower.getUsername(),
                following.getEmail(),
                null,
                NotificationTypeEnum.FOLLOW
        );

        return toFollowResponse(savedFollow);
    }

    @Override
    public FollowResponse unfollowUser(Long userId, Long currentUserId) {
        Follow follow = followRepository.findFollowsByFollowerUserIDAndFollowingUserID(currentUserId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.FOLLOW_NOT_FOUND));

        follow.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        Follow savedFollow = followRepository.save(follow);
        return toFollowResponse(savedFollow);
    }

    @Override
    public List<FollowResponse> getAllFollowersByUserId(Long currentUserId) {
        // Lấy danh sách những người theo dõi user (following_id = currentUserId)
        return followRepository.findAllByFollowingUserIDAndDeletedAtIsNull(currentUserId)
                .stream()
                .map(this::toFollowerResponse)
                .toList();
    }

    @Override
    public List<FollowResponse> getAllFollowingByUserId(Long currentUserId) {
        // Lấy danh sách những người user theo dõi (follower_id = currentUserId)
        return followRepository.findAllByFollowerUserIDAndDeletedAtIsNull(currentUserId)
                .stream()
                .map(this::toFollowingResponse)
                .toList();
    }

    private FollowResponse toFollowerResponse(Follow follow) {
        // Chỉ trả về thông tin của người theo dõi
        return FollowResponse.builder()
                .followID(follow.getFollowID())
                .followerUser(toUserResponse(follow.getFollower()))
                .createdAt(follow.getCreatedAt())
                .updatedAt(follow.getUpdatedAt())
                .deletedAt(follow.getDeletedAt())
                .build();
    }

    private FollowResponse toFollowingResponse(Follow follow) {
        // Chỉ trả về thông tin của người được theo dõi
        return FollowResponse.builder()
                .followID(follow.getFollowID())
                .followingUser(toUserResponse(follow.getFollowing()))
                .createdAt(follow.getCreatedAt())
                .updatedAt(follow.getUpdatedAt())
                .deletedAt(follow.getDeletedAt())
                .build();
    }

    private FollowResponse toFollowResponse(Follow follow) {
        // Trả về cả thông tin người theo dõi và người được theo dõi
        return FollowResponse.builder()
                .followID(follow.getFollowID())
                .followerUser(toUserResponse(follow.getFollower()))
                .followingUser(toUserResponse(follow.getFollowing()))
                .createdAt(follow.getCreatedAt())
                .updatedAt(follow.getUpdatedAt())
                .deletedAt(follow.getDeletedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userID(user.getUserID())
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .profilePic(user.getProfilePic())
                .authType(user.getAuthType().getValue())
                .build();
    }
}