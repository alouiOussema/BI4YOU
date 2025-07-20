package pi2425.bi4you.services.inters;

import pi2425.bi4you.enmus.ERole;
import pi2425.bi4you.entities.Roles;
import pi2425.bi4you.entities.User;

import java.util.List;
import java.util.Optional;

public interface IAuthService {
    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<Roles> findByName(ERole name);

    List<Roles> findAllRoles();

    User saveUser(User user);

    String forgetPassword(String email);

    String resetPassword(String token, String password);
}
