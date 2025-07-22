package pi2425.bi4you.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pi2425.bi4you.dtos.requests.ChangePasswordRequest;
import pi2425.bi4you.dtos.requests.UpdateProfileRequest;
import pi2425.bi4you.dtos.responses.MessageResponse;
import pi2425.bi4you.dtos.responses.UserResponse;
import pi2425.bi4you.security.services.UserDetailsImpl;
import pi2425.bi4you.services.inters.IUserService;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    
    private final IUserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        UserResponse userProfile = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            UserResponse updatedProfile = userService.updateUserProfile(userDetails.getId(), updateProfileRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            userService.changePassword(userDetails.getId(), changePasswordRequest);
            return ResponseEntity.ok(new MessageResponse("Mot de passe modifié avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: " + e.getMessage()));
        }
    }
}

