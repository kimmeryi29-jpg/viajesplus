package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findTop200ByOrderByOccurredAtDesc();

    List<AuditEntry> findByEntityTypeAndEntityIdOrderByOccurredAtAsc(String entityType, String entityId);
}
