package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsCategoryByCategoryName(String categoryName);
}
