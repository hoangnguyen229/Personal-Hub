package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.CommentRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.CommentResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for comment-related endpoints
 * Handles creating, retrieving, updating, and deleting comments
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * Create a new comment for a post
     * 
     * @param commentRequest the comment request containing content and post ID
     * @param userDetail authenticated user details
     * @return newly created comment response
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        CommentResponse createdComment = commentService.createComment(commentRequest, userDetail.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
    
    /**
     * Update an existing comment
     * 
     * @param commentId the ID of the comment to update
     * @param commentRequest the updated comment data
     * @param userDetail authenticated user details
     * @return the updated comment response
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        CommentResponse updatedComment = commentService.updateComment(commentId, commentRequest, userDetail.getId());
        return ResponseEntity.ok(updatedComment);
    }
    
    /**
     * Delete a comment
     * 
     * @param commentId the ID of the comment to delete
     * @param userDetail authenticated user details
     * @return success message
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponse> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        CommentResponse deletedComment = commentService.deleteComment(commentId, userDetail.getId());
        return ResponseEntity.ok(deletedComment);
    }

    /**
     * Retrieve all comments for a specific post
     *
     * @param postId the ID of the post to retrieve comments for
     * @return list of comments for the specified post have not been deleted
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId){
        List<CommentResponse> commentResponses = commentService.getAllCommentsByPostId(postId);
        return ResponseEntity.ok(commentResponses);
    }
}