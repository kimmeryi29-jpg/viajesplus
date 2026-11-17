package co.viajesplus.api;

import co.viajesplus.application.NotificationService;
import co.viajesplus.infrastructure.persistence.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/** Avisos recibidos por el usuario (T012 y T015 · NFR-VIA-003). */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Avisos")
public class NotificationController {

    public record NotificationView(Long id, String subject, String body, String relatedCode, String status,
                                   int attempts, Instant createdAt, Instant sentAt) {
        static NotificationView of(Notification n) {
            return new NotificationView(n.getId(), n.getSubject(), n.getBody(), n.getRelatedCode(), n.getStatus(),
                    n.getAttempts(), n.getCreatedAt(), n.getSentAt());
        }
    }

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    @Operation(summary = "Mis avisos", description = "Avisos por correo enviados al usuario autenticado.")
    public List<NotificationView> mine(Authentication auth) {
        return notifications.forUser(auth.getName()).stream().map(NotificationView::of).toList();
    }
}
