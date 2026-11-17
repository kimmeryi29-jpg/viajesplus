package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByProviderUsernameIgnoreCaseOrderByNameAsc(String providerUsername);

    List<Hotel> findByDestinationOrderByNameAsc(String destination);
}
