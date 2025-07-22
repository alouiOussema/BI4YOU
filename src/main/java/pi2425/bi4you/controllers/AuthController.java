package pi2425.bi4you.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pi2425.bi4you.dtos.requests.ForgetPassword;
import pi2425.bi4you.dtos.requests.LoginRequest;
import pi2425.bi4you.dtos.requests.ResetPasswordRequest;
import pi2425.bi4you.dtos.responses.JwtResponse;
import pi2425.bi4you.dtos.responses.MessageResponse;
import pi2425.bi4you.entities.User;
import pi2425.bi4you.security.jwt.JwtUtils;
import pi2425.bi4you.security.services.UserDetailsImpl;
import pi2425.bi4you.services.inters.IAuthService;
import pi2425.bi4you.services.inters.IUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    
    AuthenticationManager authenticationManager;
    IAuthService authService;
    IUserService userService;
    PasswordEncoder encoder;
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Check if user exists and is active
            User user = authService.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (!user.isActive()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Compte désactivé. Contactez l'administrateur."));
            }

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), 
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(userDetails);
            
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            authService.saveUser(user);

            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(),
                    userDetails.getUsername(), userDetails.getEmail(), roles));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur d'authentification: " + e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie!"));
    }

    @PostMapping("/forgetpassword")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetPassword fp) {
        try {
            String message = authService.forgetPassword(fp.getEmail());
            return ResponseEntity.ok(new MessageResponse(message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest rpr) {
        try {
            String message = authService.resetPassword(rpr.getToken(), rpr.getPassword());
            return ResponseEntity.ok(new MessageResponse(message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }
}

