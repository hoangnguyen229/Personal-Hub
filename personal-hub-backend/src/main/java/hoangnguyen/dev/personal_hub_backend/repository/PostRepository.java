package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Category;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Optional<Post> findByTitle(String title);
    Page<Post> findByCategory(Category category, Pageable pageable);
    Page<Post> findByTagsContaining(Tag tag, Pageable pageable);
    Page<Post> findAllByDeletedAtIsNull(Pageable pageable);
    List<Post> findByUserUserIDAndDeletedAtIsNull(Long userID);
    Page<Post> findByPostIDIn(Set<Long> postIds, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags")
    List<Post> findAllWithTags();
}
