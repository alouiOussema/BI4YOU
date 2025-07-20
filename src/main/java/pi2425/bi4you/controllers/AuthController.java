package pi2425.bi4you.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
import pi2425.bi4you.dtos.requests.SignupRequest;
import pi2425.bi4you.dtos.responses.JwtResponse;
import pi2425.bi4you.dtos.responses.MessageResponse;
import pi2425.bi4you.enmus.ERole;
import pi2425.bi4you.entities.Roles;
import pi2425.bi4you.entities.User;
import pi2425.bi4you.security.jwt.JwtUtils;
import pi2425.bi4you.security.services.UserDetailsImpl;
import pi2425.bi4you.services.inters.IAuthService;
import pi2425.bi4you.services.inters.IUserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @GetMapping("/getAllRoles")
    public ResponseEntity<List<Roles>> getAllRoles() {
        List<Roles> roles = authService.findAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(),
                userDetails.getUsername(), userDetails.getEmail(), roles));

    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (authService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (authService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // --- MODIFICATION ICI ---
        // Créez l'utilisateur en utilisant le mot de passe encodé
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()) // Utilisez le PasswordEncoder ici !
        );

        String role = signUpRequest.getRole();
        Set<Roles> roles = new HashSet<>();

        // Votre logique pour les rôles reste inchangée
        switch (role) {
            case "DirecteurGenerale" -> {
                Roles roleToAdd = authService.findByName(ERole.DirecteurGenerale)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(roleToAdd);
            }
            case "DirecteurCommercial" -> {
                Roles roleToAdd = authService.findByName(ERole.DirecteurCommercial)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(roleToAdd);
            }
            case "DirecteurMarketing" -> {
                Roles roleToAdd = authService.findByName(ERole.DirecteurMarketing)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(roleToAdd);
            }
            case "DirecteurAchats" -> {
                Roles roleToAdd = authService.findByName(ERole.DirecteurAchats)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(roleToAdd);
            }
            case "ResponsableLogistique" -> {
                Roles roleToAdd = authService.findByName(ERole.ResponsableLogistique)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(roleToAdd);
            }
            // Il serait bon d'ajouter un cas par défaut pour gérer les rôles invalides
            default -> {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Role is not valid!"));
            }
        }

        user.setRoles(roles);
        authService.saveUser(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

    @PostMapping("/forgetpassword")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetPassword fp) {
        return ResponseEntity.ok(new MessageResponse(authService.forgetPassword(fp.getEmail())));
    }

    @PutMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest rpr) {
        return ResponseEntity.ok(new MessageResponse(authService.resetPassword(rpr.getToken(), rpr.getPassword())));
    }
}