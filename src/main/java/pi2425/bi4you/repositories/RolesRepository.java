package pi2425.bi4you.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pi2425.bi4you.enmus.ERole;
import pi2425.bi4you.entities.Roles;

import java.util.Optional;


@Repository
public interface RolesRepository extends JpaRepository<Roles, Long> {

    Optional<Roles> findByName(ERole name);
}
