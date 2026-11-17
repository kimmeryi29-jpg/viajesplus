package co.viajesplus.application;

import co.viajesplus.infrastructure.persistence.Notification;
import co.viajesplus.infrastructure.persistence.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * T012 · Notificación al cliente, al guía y al conductor (ADR-004, NFR-VIA-003). Escucha el evento después del
 * commit: si el cambio de estado se revierte, no se avisa. Hace hasta tres intentos; si fallan, el aviso queda
 * FALLIDO para reenviarlo y la decisión no se revierte.
 */
@Service
public class NotificationService {

    static final int MAX_ATTEMPTS = 3;

    private final NotificationRepository repository;
    private final EmailSender sender;

    public NotificationService(NotificationRepository repository, EmailSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(NotificationRequested event) {
        Notification n = repository.save(new Notification(event.recipient(), event.subject(), event.body(),
                event.relatedCode()));
        while (!Notification.ENVIADO.equals(n.getStatus()) && n.getAttempts() < MAX_ATTEMPTS) {
            boolean ok;
            try {
                ok = sender.send(n);
            } catch (RuntimeException e) {
                ok = false;
            }
            n.attempt(ok);
        }
        repository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> forUser(String username) {
        return repository.findTop50ByRecipientUsernameIgnoreCaseOrderByCreatedAtDesc(username);
    }
}
