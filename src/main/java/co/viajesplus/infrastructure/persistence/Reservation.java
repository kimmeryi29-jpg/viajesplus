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
}
