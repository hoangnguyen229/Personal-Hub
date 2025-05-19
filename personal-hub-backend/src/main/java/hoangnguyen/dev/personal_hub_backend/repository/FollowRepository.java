package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Follow;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findFollowsByFollowerUserIDAndFollowingUserID(Long followerID, Long followingID);
    List<Follow> findAllByFollowerUserIDAndDeletedAtIsNull(Long followerID);
    List<Follow> findAllByFollowingUserIDAndDeletedAtIsNull(Long followingID);
}
