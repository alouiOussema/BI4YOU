package pi2425.bi4you.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pi2425.bi4you.dtos.requests.CreateUserRequest;
import pi2425.bi4you.dtos.requests.UpdateUserRequest;
import pi2425.bi4you.dtos.responses.MessageResponse;
import pi2425.bi4you.dtos.responses.UserResponse;
import pi2425.bi4you.entities.Roles;
import pi2425.bi4you.entities.User;
import pi2425.bi4you.services.inters.IAuthService;
import pi2425.bi4you.services.inters.IUserService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final IAuthService authService;
    private final IUserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        try {
            UserResponse createdUser = userService.createUser(createUserRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        try {
            UserResponse updatedUser = userService.updateUser(id, updateUserRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("Utilisateur supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(new MessageResponse("Utilisateur activé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(new MessageResponse("Utilisateur désactivé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id) {
        try {
            String newPassword = userService.resetUserPassword(id);
            return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé. Nouveau mot de passe: " + newPassword));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Roles>> getAllRoles() {
        List<Roles> roles = authService.findAllRoles();
        return ResponseEntity.ok(roles);
    }
}

