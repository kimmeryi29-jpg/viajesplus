package co.viajesplus.application;

import co.viajesplus.infrastructure.persistence.AuditEntry;
import co.viajesplus.infrastructure.persistence.AuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * T005 · AuditService sobre la tabla audit_log (creada en la migración V1, T003).
 * REQ-VIA-011: cada cambio de estado de una reserva queda con usuario, fecha-hora UTC y motivo.
 * Se ejecuta dentro de la misma transacción del cambio: si la auditoría falla, el cambio no se guarda.
 */
@Service
public class AuditService {

    public static final String RESERVATION = "RESERVA";

    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AuditEntry recordStatusChange(String entityId, String action, String fromStatus, String toStatus,
                                         String username, String reason) {
        return repository.save(new AuditEntry(RESERVATION, entityId, action, fromStatus, toStatus, username, reason));
    }

    @Transactional(readOnly = true)
    public List<AuditEntry> latest() {
        return repository.findTop200ByOrderByOccurredAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditEntry> forReservation(String code) {
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtAsc(RESERVATION, code);
    }
}
