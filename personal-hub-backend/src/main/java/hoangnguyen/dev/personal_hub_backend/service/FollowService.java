package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.request.FollowRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.FollowResponse;

import java.util.List;

public interface FollowService {
    FollowResponse followUser(FollowRequest request, Long currentUserId );
    FollowResponse unfollowUser(Long userId, Long currentUserId);
    List<FollowResponse> getAllFollowersByUserId(Long currentUserId);
    List<FollowResponse> getAllFollowingByUserId(Long currentUserId);
}
