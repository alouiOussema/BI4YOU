package pi2425.bi4you.services.impls;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi2425.bi4you.enmus.ERole;
import pi2425.bi4you.entities.PasswordResetToken;
import pi2425.bi4you.entities.Roles;
import pi2425.bi4you.entities.User;
import pi2425.bi4you.repositories.PasswordResetTokenRepository;
import pi2425.bi4you.repositories.RolesRepository;
import pi2425.bi4you.repositories.UserRepository;
import pi2425.bi4you.services.EmailService;
import pi2425.bi4you.services.inters.IAuthService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class AuthService implements IAuthService {
    
    final UserRepository userRepo;
    final RolesRepository roleRepo;
    final PasswordResetTokenRepository tokenRepo;
    final PasswordEncoder encoder;
    final EmailService emailService;
    
    @Value("${app.passwordResetTokenExpirationMs}")
    private long passwordResetTokenExpirationMs;

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Roles> findByName(ERole name) {
        return roleRepo.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Roles> findAllRoles() {
        return roleRepo.findAll();
    }

    @Override
    public User saveUser(User user) {
        return userRepo.save(user);
    }

    @Override
    public String forgetPassword(String email) {
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Aucun compte associé à cet email");
        }

        User user = userOptional.get();
        
        if (!user.isActive()) {
            throw new RuntimeException("Compte désactivé. Contactez l'administrateur.");
        }

        // Delete existing tokens for this user
        tokenRepo.deleteByUser(user);

        // Generate new token
        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(passwordResetTokenExpirationMs / 1000);
        
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        tokenRepo.save(resetToken);

        // Send email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return "Un email de réinitialisation a été envoyé à votre adresse";
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    @Override
    public String resetPassword(String token, String password) {
        Optional<PasswordResetToken> tokenOptional = tokenRepo.findByToken(token);

        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("Token invalide");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.isExpired()) {
            tokenRepo.delete(resetToken);
            throw new RuntimeException("Token expiré");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token déjà utilisé");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(password));
        user.setFirstLogin(false);
        userRepo.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepo.save(resetToken);

        // Clean up expired tokens
        tokenRepo.deleteExpiredTokens(LocalDateTime.now());

        return "Mot de passe réinitialisé avec succès";
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

