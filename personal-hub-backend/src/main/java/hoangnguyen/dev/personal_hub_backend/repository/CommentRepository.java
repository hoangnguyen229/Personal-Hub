package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.dto.response.CommentResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Count the number of comments with the same content by a specific user for a specific post
     * 
     * @param userId the user ID
     * @param postId the post ID
     * @param content the comment content
     * @return the count of duplicate comments
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.userID = :userId AND c.post.postID = :postId AND c.content = :content AND c.deletedAt IS NULL")
    int countDuplicateCommentsByUser(
            @Param("userId") Long userId,
            @Param("postId") Long postId,
            @Param("content") String content);


    @Query("SELECT c FROM Comment c WHERE c.post.postID = :postId AND c.deletedAt IS NULL")
    List<Comment> findAllByPostIdAndDeletedAtIsNull(@Param("postId") Long postId);
}
