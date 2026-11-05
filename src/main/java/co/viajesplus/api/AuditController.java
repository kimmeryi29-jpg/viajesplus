package co.viajesplus.api;

import co.viajesplus.application.AuditService;
import co.viajesplus.infrastructure.persistence.AuditEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/** HU-08 · REQ-VIA-011: auditoría consultable por el Gerente y el Administrador. */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Auditoría")
public class AuditController {

    public record AuditView(String entityType, String entityId, String action, String fromStatus, String toStatus,
                            String username, Instant occurredAt, String reason) {
        static AuditView of(AuditEntry e) {
            return new AuditView(e.getEntityType(), e.getEntityId(), e.getAction(), e.getFromStatus(),
                    e.getToStatus(), e.getUsername(), e.getOccurredAt(), e.getReason());
        }
    }

    private final AuditService audit;

    public AuditController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping
    @Operation(summary = "Últimos cambios de estado", description = "Roles Gerente y Administrador. Filtra por reserva con ?reservation=RES-00001.")
    public List<AuditView> list(@RequestParam(required = false) String reservation) {
        List<AuditEntry> entries = reservation == null ? audit.latest() : audit.forReservation(reservation);
        return entries.stream().map(AuditView::of).toList();
    }
}
