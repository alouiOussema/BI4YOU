package pi2425.bi4you.services.inters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pi2425.bi4you.dtos.requests.ChangePasswordRequest;
import pi2425.bi4you.dtos.requests.CreateUserRequest;
import pi2425.bi4you.dtos.requests.UpdateProfileRequest;
import pi2425.bi4you.dtos.requests.UpdateUserRequest;
import pi2425.bi4you.dtos.responses.UserResponse;
import pi2425.bi4you.entities.User;

import java.util.List;

public interface IUserService {
    
    // Admin functions
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse createUser(CreateUserRequest createUserRequest);
    UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest);
    void deleteUser(Long id);
    void activateUser(Long id);
    void deactivateUser(Long id);
    String resetUserPassword(Long id);
    
    // User profile functions
    UserResponse updateUserProfile(Long id, UpdateProfileRequest updateProfileRequest);
    void changePassword(Long id, ChangePasswordRequest changePasswordRequest);
    
    // Utility functions
    User findUserById(Long id);
    String generateRandomPassword();
}

