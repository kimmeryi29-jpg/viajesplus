package co.viajesplus.infrastructure.persistence;

import co.viajesplus.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClientUsernameOrderByCreatedAtDesc(String clientUsername);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Query(value = "SELECT nextval('reservation_code_seq')", nativeQuery = true)
    long nextCodeNumber();

    List<Reservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);

    List<Reservation> findByGuideUsernameIgnoreCaseOrDriverUsernameIgnoreCase(String guide, String driver);

    /** RN-06: otras reservas de una salida distinta, el mismo día, con este guía. */
    @Query(value = "SELECT COUNT(*) FROM reservation r JOIN departure d ON d.id = r.departure_id "
            + "WHERE lower(r.guide_username) = lower(:guide) AND d.departure_date = :date AND d.id <> :departureId",
            nativeQuery = true)
    long countGuideConflicts(@Param("guide") String guide, @Param("date") LocalDate date,
                             @Param("departureId") Long departureId);

    /** RN-06: otras reservas de una salida distinta, el mismo día, con este conductor. */
    @Query(value = "SELECT COUNT(*) FROM reservation r JOIN departure d ON d.id = r.departure_id "
            + "WHERE lower(r.driver_username) = lower(:driver) AND d.departure_date = :date AND d.id <> :departureId",
            nativeQuery = true)
    long countDriverConflicts(@Param("driver") String driver, @Param("date") LocalDate date,
                              @Param("departureId") Long departureId);
}
