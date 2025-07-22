package pi2425.bi4you.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 20, message = "Le nom d'utilisateur doit contenir entre 3 et 20 caractères")
    private String username;
    
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 50, message = "L'email ne peut pas dépasser 50 caractères")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;
    
    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(max = 50, message = "Le nom de famille ne peut pas dépasser 50 caractères")
    private String lastName;
    
    private Set<String> roles;
    
    private boolean sendWelcomeEmail = true;
}

