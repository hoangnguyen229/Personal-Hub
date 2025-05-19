package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.request.CommentRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    /**
     * Create a new comment for a post
     * @param commentRequest the comment request containing comment content and post ID
     * @param currentUserId the ID of the currently authenticated user
     * @return the created comment response
     */
    CommentResponse createComment(CommentRequest commentRequest, Long currentUserId);
    
    /**
     * Update an existing comment
     * @param commentId the ID of the comment to update
     * @param commentRequest the updated comment data
     * @param currentUserId the ID of the currently authenticated user
     * @return the updated comment response
     */
    CommentResponse updateComment(Long commentId, CommentRequest commentRequest, Long currentUserId);
    
    /**
     * Delete a comment
     * @param commentId the ID of the comment to delete
     * @param currentUserId the ID of the currently authenticated user
     * @return true if the comment was successfully deleted
     */
    CommentResponse deleteComment(Long commentId, Long currentUserId);

    /**
     * Retrieve all comments for a specific post
     * @param postId the ID of the post to retrieve comments for
     * @return the response containing all comments for the specified post
     */
    List<CommentResponse> getAllCommentsByPostId(Long postId);
}