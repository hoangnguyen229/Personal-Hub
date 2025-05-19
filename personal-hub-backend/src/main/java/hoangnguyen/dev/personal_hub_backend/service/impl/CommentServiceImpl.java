package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.config.RabbitMQConfig;
import hoangnguyen.dev.personal_hub_backend.dto.request.CommentRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.CommentResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Comment;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.NotificationTypeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.CommentRepository;
import hoangnguyen.dev.personal_hub_backend.repository.PostRepository;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.CommentService;
import hoangnguyen.dev.personal_hub_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final int MAX_DUPLICATE_COMMENTS = 3;
    
    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest commentRequest, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));
        
        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));

        getMaxDuplicateComments(currentUserId, commentRequest.getPostId(), commentRequest.getContent());
        
        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setUser(user);
        comment.setPost(post);
        
        Comment savedComment = commentRepository.save(comment);

        if(!post.getUser().getUserID().equals(currentUserId)) {
            notificationService.sendNotification(
                    comment.getUser().getUsername(),
                    post.getUser().getEmail(),
                    post.getPostID(),
                    NotificationTypeEnum.COMMENT
            );
        }


        return mapToCommentResponse(savedComment);
    }
    
    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest commentRequest, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.COMMENT_NOT_FOUND));
                
        if (!comment.getUser().getUserID().equals(currentUserId)) {
            throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
        }
        
        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));

        getMaxDuplicateComments(currentUserId, commentRequest.getPostId(), commentRequest.getContent());
        
        comment.setContent(commentRequest.getContent());
        
        Comment updatedComment = commentRepository.save(comment);
        
        return mapToCommentResponse(updatedComment);
    }
    
    @Override
    @Transactional
    public CommentResponse deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.COMMENT_NOT_FOUND));

        Post post = postRepository.findById(comment.getPost().getPostID())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));

        // Verify the post belongs to the user
        if(!post.getUser().getUserID().equals(currentUserId)){
            if (!comment.getUser().getUserID().equals(currentUserId)) {
                throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
            }
        }

        // Perform soft delete by setting the deletedAt timestamp
        comment.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        Comment deletedComment = commentRepository.save(comment);

        return mapToCommentResponse(comment);
    }

    @Override
    public List<CommentResponse> getAllCommentsByPostId(Long postId) {
        return commentRepository.findAllByPostIdAndDeletedAtIsNull(postId)
                .stream()
                .map(this::mapToCommentResponse)
                .toList();
    }

    private void getMaxDuplicateComments(Long currentUserId, Long postId, String content){
        int duplicateCount = commentRepository.countDuplicateCommentsByUser(currentUserId, postId, content);
        if (duplicateCount >= MAX_DUPLICATE_COMMENTS) {
            throw new ApiException(ErrorCodeEnum.MAX_DUPLICATE_COMMENTS);
        }
    }
    
    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .commentID(comment.getCommentID())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .deletedAt(comment.getDeletedAt())
                .user(mapToUserResponse(comment.getUser()))
                .build();
    }

    private UserResponse mapToUserResponse(User user){
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePic(user.getProfilePic())
                .build();
    }
}