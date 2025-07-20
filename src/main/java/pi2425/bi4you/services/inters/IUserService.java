package pi2425.bi4you.services.inters;

import pi2425.bi4you.entities.User;

import java.util.List;

public interface IUserService {
    List<User> getAllUsers();

    User getUser(Long id);

    void updateUser(Long id, User _user);

    void deleteUser(Long id);
}
