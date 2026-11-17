package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HotelAvailabilityRepository extends JpaRepository<HotelAvailability, Long> {

    Optional<HotelAvailability> findByHotelIdAndDepartureId(Long hotelId, Long departureId);

    List<HotelAvailability> findByDepartureId(Long departureId);

    List<HotelAvailability> findByHotelIdIn(Collection<Long> hotelIds);

    boolean existsByDepartureIdAndConfirmedTrue(Long departureId);
}
