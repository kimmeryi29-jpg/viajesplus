package co.viajesplus.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Salida de un paquete en una fecha, con su cupo (RN-03). */
@Entity
@Table(name = "departure")
public class Departure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(nullable = false)
    private short capacity;

    @Column(name = "occupied_seats", nullable = false)
    private short occupiedSeats;

    protected Departure() {
    }

    public Long getId() {
        return id;
    }

    public Long getPackageId() {
        return packageId;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public int getAvailableSeats() {
        return capacity - occupiedSeats;
    }
}
