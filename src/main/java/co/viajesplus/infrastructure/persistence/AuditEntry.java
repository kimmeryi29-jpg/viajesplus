package co.viajesplus.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Registro de la tabla audit_log (REQ-VIA-011). Solo se inserta; nunca se modifica. */
@Entity
@Table(name = "audit_log")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String action;

    @Column(name = "from_status")
    private String fromStatus;

    @Column(name = "to_status")
    private String toStatus;

    @Column(nullable = false)
    private String username;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    private String reason;

    protected AuditEntry() {
    }

    public AuditEntry(String entityType, String entityId, String action, String fromStatus, String toStatus,
                      String username, String reason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.username = username;
        this.reason = reason;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public String getUsername() {
        return username;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getReason() {
        return reason;
    }
}
