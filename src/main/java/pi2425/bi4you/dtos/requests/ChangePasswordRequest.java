package pi2425.bi4you.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String currentPassword;
    
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, max = 40, message = "Le mot de passe doit contenir entre 6 et 40 caractères")
    private String newPassword;
    
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
}

