package pi2425.bi4you.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level= AccessLevel.PRIVATE)
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    String username;

    @NotBlank
    @Size(max = 50)
    @Email
    String email;
    @NotBlank
    String role;

    @NotBlank
    @Size(min = 6, max = 40)
    String password;

    public SignupRequest() {
    }

    public @NotBlank @Size(min = 3, max = 20) String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank @Size(min = 3, max = 20) String username) {
        this.username = username;
    }

    public @NotBlank @Size(max = 50) @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Size(max = 50) @Email String email) {
        this.email = email;
    }

    public @NotBlank String getRole() {
        return role;
    }

    public void setRole(@NotBlank String role) {
        this.role = role;
    }

    public @NotBlank @Size(min = 6, max = 40) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(min = 6, max = 40) String password) {
        this.password = password;
    }

    public SignupRequest(String username, String email, String role, String password) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
    }
}