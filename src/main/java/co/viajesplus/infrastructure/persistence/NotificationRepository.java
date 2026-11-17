package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByRecipientUsernameIgnoreCaseOrderByCreatedAtDesc(String recipientUsername);

    List<Notification> findByRelatedCodeOrderByCreatedAtAsc(String relatedCode);
}
