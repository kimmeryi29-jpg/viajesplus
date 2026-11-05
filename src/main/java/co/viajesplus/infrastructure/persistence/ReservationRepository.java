package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClientUsernameOrderByCreatedAtDesc(String clientUsername);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Query(value = "SELECT nextval('reservation_code_seq')", nativeQuery = true)
    long nextCodeNumber();
}
