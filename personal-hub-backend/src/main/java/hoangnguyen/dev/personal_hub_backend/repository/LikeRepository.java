package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserUserIDAndPostPostIDAndDeletedAtIsNull(Long userID, Long postID);
    Optional<Like> findByUserUserIDAndPostPostID(Long userId, Long postId);
    List<Like> findAllByPostPostIDAndDeletedAtIsNull(Long postId);
    long countByPostPostIDAndDeletedAtIsNull(Long postId);
}
