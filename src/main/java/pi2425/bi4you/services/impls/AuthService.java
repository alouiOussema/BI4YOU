package pi2425.bi4you.services.impls;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pi2425.bi4you.enmus.ERole;
import pi2425.bi4you.entities.Roles;
import pi2425.bi4you.entities.User;
import pi2425.bi4you.repositories.RolesRepository;
import pi2425.bi4you.repositories.UserRepository;
import pi2425.bi4you.services.inters.IAuthService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
public class AuthService implements IAuthService {
    UserRepository userRepo;
    RolesRepository roleRepo;
    PasswordEncoder encoder;

    @Override
    public Boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
    @Override
    public Optional<Roles> findByName(ERole name) {
        return roleRepo.findByName(name);
    }

    @Override
    public List<Roles> findAllRoles() { return roleRepo.findAll();}
    @Override
    public User saveUser(User user) {
        user.setCreationDate(new Date(System.currentTimeMillis()));
        user.setPassword(encoder.encode(user.getPassword()));

        return userRepo.save(user); }

    @Override
    public String forgetPassword(String email) {

        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "Invalid email.";
        }

        User user = userOptional.get();
        String token = generateToken();
        user.setToken(token);
        user.setTokenCreationDate(LocalDateTime.now());

        String subject = "Forget Password Request";
        String body = "Your verification code is: " + token;

        //sendEmail(email, subject, body);
        userRepo.save(user);

        return user.getToken();
    }

    @Override
    public String resetPassword(String token, String password) {

        Optional<User> userOptional = userRepo.findByToken(token);

        if (userOptional.isEmpty()) {
            return "Invalid token.";
        }

        LocalDateTime tokenCreationDate = userOptional.get().getTokenCreationDate();

        if (isTokenExpired(tokenCreationDate)) {
            return "Token expired.";

        }

        User user = userOptional.get();
        user.setPassword(encoder.encode(password));
        user.setToken(null);
        user.setTokenCreationDate(null);

        userRepo.save(user);

        return "Your password successfully updated.";
    }
    private String generateToken() {
        return String.valueOf(UUID.randomUUID()) +
                UUID.randomUUID();
    }
    private boolean isTokenExpired(LocalDateTime tokenCreationDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(tokenCreationDate, now);
        return diff.toMinutes() >= 30;
    }

    /*public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("AgriGoodness@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        emailSender.send(message);
        System.out.println("Mail Sent successfully...");
    }*/
}
