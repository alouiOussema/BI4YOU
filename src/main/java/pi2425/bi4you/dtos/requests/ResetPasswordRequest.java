package pi2425.bi4you.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level= AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank

    String token;

    @NotBlank
    String password;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public @NotBlank String getToken() {
        return token;
    }

    public void setToken(@NotBlank String token) {
        this.token = token;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }
}
