package hoangnguyen.dev.personal_hub_backend.repository;

import hoangnguyen.dev.personal_hub_backend.entity.Notification;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findNotificationByUserAndDeletedAtIsNull(User user);
}
