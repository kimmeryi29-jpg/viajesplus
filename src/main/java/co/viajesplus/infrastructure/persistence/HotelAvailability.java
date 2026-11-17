package co.viajesplus.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/** Disponibilidad y tarifa que el proveedor confirma para un hotel en una salida (RN-05). */
@Entity
@Table(name = "hotel_availability")
public class HotelAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "departure_id", nullable = false)
    private Long departureId;

    @Column(nullable = false)
    private short rooms;

    @Column(name = "rate_per_night", nullable = false)
    private BigDecimal ratePerNight;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected HotelAvailability() {
    }

    public HotelAvailability(Long hotelId, Long departureId) {
        this.hotelId = hotelId;
        this.departureId = departureId;
        this.ratePerNight = BigDecimal.ZERO;
    }

    public void update(int rooms, BigDecimal ratePerNight, boolean confirmed, String by) {
        this.rooms = (short) rooms;
        this.ratePerNight = ratePerNight;
        this.confirmed = confirmed;
        this.updatedBy = by;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public Long getDepartureId() {
        return departureId;
    }

    public int getRooms() {
        return rooms;
    }

    public BigDecimal getRatePerNight() {
        return ratePerNight;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
