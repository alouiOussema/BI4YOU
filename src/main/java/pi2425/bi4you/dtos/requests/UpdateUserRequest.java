package pi2425.bi4you.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    
    @Size(max = 50, message = "L'email ne peut pas dépasser 50 caractères")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;
    
    @Size(max = 50, message = "Le nom de famille ne peut pas dépasser 50 caractères")
    private String lastName;
    
    private Set<String> roles;
    
    private Boolean active;
}

