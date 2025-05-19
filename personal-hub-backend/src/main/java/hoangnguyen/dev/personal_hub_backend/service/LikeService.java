package hoangnguyen.dev.personal_hub_backend.service;


import hoangnguyen.dev.personal_hub_backend.dto.request.LikeRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.LikeResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Like;

import java.util.List;

public interface LikeService {
    LikeResponse likePost(LikeRequest likeRequest, Long currentUserId);

    LikeResponse unlikePost(Long postId, Long currentUserId);

    List<LikeResponse> getAllLikesByPostId(Long postId);

    long getLikeCountByPostId(Long postId);

}
