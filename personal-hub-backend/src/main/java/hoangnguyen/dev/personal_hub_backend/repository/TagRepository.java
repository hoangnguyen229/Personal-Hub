package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String name);
    Optional<Tag> findBySlug(String slug);
}
