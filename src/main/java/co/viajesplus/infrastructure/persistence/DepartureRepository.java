package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepartureRepository extends JpaRepository<Departure, Long> {

    List<Departure> findByPackageIdOrderByDepartureDateAsc(Long packageId);

    Optional<Departure> findByPackageIdAndDepartureDate(Long packageId, LocalDate departureDate);

    /**
     * Ocupa plazas solo si caben (ADR-002). La condición y el bloqueo de fila de PostgreSQL hacen que, con dos
     * operaciones simultáneas por la última plaza, solo una actualice; la restricción ck_departure_cupo es la
     * última protección. Devuelve 1 si ocupó las plazas y 0 si no había cupo. Desde el Sprint 2 (spec v1.0.1, RN-02) lo usa la solicitud: las reservas PENDIENTE y CONFIRMADA ocupan cupo.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE departure SET occupied_seats = occupied_seats + :seats "
            + "WHERE id = :id AND occupied_seats + :seats <= capacity", nativeQuery = true)
    int occupySeats(@Param("id") Long id, @Param("seats") int seats);

    /** Libera plazas al rechazar (Sprint 2) o cancelar (Sprint 3) una reserva que ocupaba cupo (RN-02). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE departure SET occupied_seats = GREATEST(occupied_seats - :seats, 0) WHERE id = :id",
            nativeQuery = true)
    int releaseSeats(@Param("id") Long id, @Param("seats") int seats);
}
