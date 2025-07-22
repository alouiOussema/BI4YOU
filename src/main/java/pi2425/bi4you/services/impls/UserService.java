package pi2425.bi4you.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi2425.bi4you.dtos.requests.ChangePasswordRequest;
import pi2425.bi4you.dtos.requests.CreateUserRequest;
import pi2425.bi4you.dtos.requests.UpdateProfileRequest;
import pi2425.bi4you.dtos.requests.UpdateUserRequest;
import pi2425.bi4you.dtos.responses.UserResponse;
import pi2425.bi4you.enmus.ERole;
import pi2425.bi4you.entities.Roles;
import pi2425.bi4you.entities.User;
import pi2425.bi4you.repositories.UserRepository;
import pi2425.bi4you.services.EmailService;
import pi2425.bi4you.services.inters.IAuthService;
import pi2425.bi4you.services.inters.IUserService;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserService {
    
    private final UserRepository userRepository;
    private final IAuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        // Check if username already exists
        if (authService.existsByUsername(createUserRequest.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà");
        }
        
        // Check if email already exists
        if (authService.existsByEmail(createUserRequest.getEmail())) {
            throw new RuntimeException("L'email existe déjà");
        }
        
        // Generate random password
        String temporaryPassword = generateRandomPassword();
        
        // Create user
        User user = new User(
                createUserRequest.getUsername(),
                createUserRequest.getEmail(),
                passwordEncoder.encode(temporaryPassword),
                createUserRequest.getFirstName(),
                createUserRequest.getLastName()
        );
        
        // Set roles
        Set<Roles> roles = new HashSet<>();
        if (createUserRequest.getRoles() != null && !createUserRequest.getRoles().isEmpty()) {
            for (String roleName : createUserRequest.getRoles()) {
                try {
                    ERole eRole = ERole.valueOf( roleName);
                    Roles role = authService.findByName(eRole)
                            .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Rôle invalide: " + roleName);
                }
            }
        } else {
            // Default role if none specified
            Roles defaultRole = authService.findByName(ERole.DirecteurCommercial)
                    .orElseThrow(() -> new RuntimeException("Rôle par défaut non trouvé"));
            roles.add(defaultRole);
        }
        
        user.setRoles(roles);
        User savedUser = authService.saveUser(user);
        
        // Send welcome email if requested
        if (createUserRequest.isSendWelcomeEmail()) {
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), temporaryPassword);
            } catch (Exception e) {
                // Log error but don't fail user creation
                System.err.println("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
            }
        }
        
        return convertToUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = findUserById(id);
        
        // Update email if provided and different
        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().equals(user.getEmail())) {
            if (authService.existsByEmail(updateUserRequest.getEmail())) {
                throw new RuntimeException("L'email existe déjà");
            }
            user.setEmail(updateUserRequest.getEmail());
        }
        
        // Update other fields
        if (updateUserRequest.getFirstName() != null) {
            user.setFirstName(updateUserRequest.getFirstName());
        }
        if (updateUserRequest.getLastName() != null) {
            user.setLastName(updateUserRequest.getLastName());
        }
        if (updateUserRequest.getActive() != null) {
            user.setActive(updateUserRequest.getActive());
        }
        
        // Update roles if provided
        if (updateUserRequest.getRoles() != null) {
            Set<Roles> roles = new HashSet<>();
            for (String roleName : updateUserRequest.getRoles()) {
                try {
                    ERole eRole = ERole.valueOf("ROLE_" + roleName.toUpperCase());
                    Roles role = authService.findByName(eRole)
                            .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Rôle invalide: " + roleName);
                }
            }
            user.setRoles(roles);
        }
        
        User updatedUser = authService.saveUser(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    @Override
    public void activateUser(Long id) {
        User user = findUserById(id);
        user.setActive(true);
        authService.saveUser(user);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = findUserById(id);
        user.setActive(false);
        authService.saveUser(user);
    }

    @Override
    public String resetUserPassword(Long id) {
        User user = findUserById(id);
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(true);
        authService.saveUser(user);
        
        // Send email with new password
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), newPassword);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage());
        }
        
        return newPassword;
    }

    @Override
    public UserResponse updateUserProfile(Long id, UpdateProfileRequest updateProfileRequest) {
        User user = findUserById(id);
        
        // Update email if provided and different
        if (updateProfileRequest.getEmail() != null && !updateProfileRequest.getEmail().equals(user.getEmail())) {
            if (authService.existsByEmail(updateProfileRequest.getEmail())) {
                throw new RuntimeException("L'email existe déjà");
            }
            user.setEmail(updateProfileRequest.getEmail());
        }
        
        // Update other fields
        if (updateProfileRequest.getFirstName() != null) {
            user.setFirstName(updateProfileRequest.getFirstName());
        }
        if (updateProfileRequest.getLastName() != null) {
            user.setLastName(updateProfileRequest.getLastName());
        }
        
        User updatedUser = authService.saveUser(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest changePasswordRequest) {
        User user = findUserById(id);
        
        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }
        
        // Verify password confirmation
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new RuntimeException("La confirmation du mot de passe ne correspond pas");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setFirstLogin(false);
        authService.saveUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
    }

    @Override
    public String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return password.toString();
    }
    
    private UserResponse convertToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roleNames,
                user.isActive(),
                user.getCreationDate() != null ? user.getCreationDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null,
                user.getLastLogin()
        );
    }
}

