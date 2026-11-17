package co.viajesplus.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import co.viajesplus.domain.Role;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    List<AppUser> findByRoleOrderByFullNameAsc(Role role);
}
