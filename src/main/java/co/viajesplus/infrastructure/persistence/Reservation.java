package co.viajesplus.infrastructure.persistence;

import co.viajesplus.domain.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "client_username", nullable = false)
    private String clientUsername;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "departure_id", nullable = false)
    private Long departureId;

    @Column(nullable = false)
    private short travelers;

    @Column(name = "price_per_person", nullable = false)
    private BigDecimal pricePerPerson;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_by")
    private String decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(name = "guide_username")
    private String guideUsername;

    @Column(name = "driver_username")
    private String driverUsername;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    protected Reservation() {
    }

    public Reservation(String code, String clientUsername, Long packageId, Long departureId, int travelers,
                       BigDecimal pricePerPerson, BigDecimal total) {
        this.code = code;
        this.clientUsername = clientUsername;
        this.packageId = packageId;
        this.departureId = departureId;
        this.travelers = (short) travelers;
        this.pricePerPerson = pricePerPerson;
        this.total = total;
        this.status = ReservationStatus.PENDIENTE;
        this.createdAt = Instant.now();
    }

    /** REQ-VIA-007: la decisión del agente pasa la reserva a CONFIRMADA. */
    public void confirm(String agent) {
        this.status = ReservationStatus.CONFIRMADA;
        this.decidedBy = agent;
        this.decidedAt = Instant.now();
    }

    /** REQ-VIA-008: el rechazo exige motivo y deja la reserva en RECHAZADA. */
    public void reject(String agent, String reason) {
        this.status = ReservationStatus.RECHAZADA;
        this.decidedBy = agent;
        this.decidedAt = Instant.now();
        this.rejectionReason = reason;
    }

    /** REQ-VIA-009: hotel, guía y conductor asignados por el coordinador. */
    public void assign(Long hotelId, String guide, String driver) {
        this.hotelId = hotelId;
        this.guideUsername = guide;
        this.driverUsername = driver;
        this.assignedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public Long getPackageId() {
        return packageId;
    }

    public Long getDepartureId() {
        return departureId;
    }

    public int getTravelers() {
        return travelers;
    }

    public BigDecimal getPricePerPerson() {
        return pricePerPerson;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public String getGuideUsername() {
        return guideUsername;
    }

    public String getDriverUsername() {
        return driverUsername;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
