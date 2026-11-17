package co.viajesplus.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Aviso por correo (ADR-004). Guarda el estado del envío y los intentos. */
@Entity
@Table(name = "notification")
public class Notification {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String ENVIADO = "ENVIADO";
    public static final String FALLIDO = "FALLIDO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Column(nullable = false)
    private String channel = "CORREO";

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String body;

    @Column(name = "related_code")
    private String relatedCode;

    @Column(nullable = false)
    private String status = PENDIENTE;

    @Column(nullable = false)
    private short attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected Notification() {
    }

    public Notification(String recipientUsername, String subject, String body, String relatedCode) {
        this.recipientUsername = recipientUsername;
        this.subject = subject;
        this.body = body;
        this.relatedCode = relatedCode;
        this.createdAt = Instant.now();
    }

    public void attempt(boolean ok) {
        attempts++;
        if (ok) {
            status = ENVIADO;
            sentAt = Instant.now();
        } else {
            status = attempts >= 3 ? FALLIDO : PENDIENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getChannel() {
        return channel;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getRelatedCode() {
        return relatedCode;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
