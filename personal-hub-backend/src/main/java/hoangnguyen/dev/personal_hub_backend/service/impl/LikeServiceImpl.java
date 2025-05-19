package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.dto.request.LikeRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.LikeResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Like;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.NotificationTypeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.LikeRepository;
import hoangnguyen.dev.personal_hub_backend.repository.PostRepository;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.LikeService;
import hoangnguyen.dev.personal_hub_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public LikeResponse likePost(LikeRequest likeRequest, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        Post post = postRepository.findById(likeRequest.getPostId())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));

        if(likeRepository.findByUserUserIDAndPostPostIDAndDeletedAtIsNull(currentUserId, likeRequest.getPostId()).isPresent()) {
            throw new ApiException(ErrorCodeEnum.ALREADY_LIKED);
        }

        Like like;
        Optional<Like> deletedLike = likeRepository.findByUserUserIDAndPostPostID(currentUserId, likeRequest.getPostId());
        if(deletedLike.isPresent()) {
            like = deletedLike.get();
            like.setDeletedAt(null);
            like.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        }
        else{
            like = new Like();
            like.setUser(user);
            like.setPost(post);
        }

        Like savedLike = likeRepository.save(like);

        if(!post.getUser().getUserID().equals(currentUserId)) {
            notificationService.sendNotification(
                    like.getUser().getUsername(),
                    post.getUser().getEmail(),
                    post.getPostID(),
                    NotificationTypeEnum.LIKE
            );
        }

        return mapToLikeResponse(savedLike);
    }

    @Override
    @Transactional
    public LikeResponse unlikePost(Long postId, Long currentUserId) {
        Like like = likeRepository.findByUserUserIDAndPostPostID(currentUserId, postId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.LIKE_NOT_FOUND)
        );

        like.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        Like savedUnlike = likeRepository.save(like);

        return mapToLikeResponse(savedUnlike);
    }

    @Override
    public List<LikeResponse> getAllLikesByPostId(Long postId) {
        return likeRepository.findAllByPostPostIDAndDeletedAtIsNull(postId)
                .stream()
                .map(this::mapToLikeResponse)
                .toList();
    }

    @Override
    public long getLikeCountByPostId(Long postId) {
        return likeRepository.countByPostPostIDAndDeletedAtIsNull(postId);
    }

    private LikeResponse mapToLikeResponse(Like like) {
        return LikeResponse.builder()
                .likeID(like.getLikeID())
                .postID(like.getPost().getPostID())
                .userID(like.getUser().getUserID())
                .createdAt(like.getCreatedAt())
                .deletedAt(like.getDeletedAt())
                .build();
    }
}
