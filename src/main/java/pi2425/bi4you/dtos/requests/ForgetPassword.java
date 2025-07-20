package pi2425.bi4you.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level= AccessLevel.PRIVATE)
public class ForgetPassword {
    @NotBlank
    String email;

    public @NotBlank String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank String email) {
        this.email = email;
    }

    public ForgetPassword(String email) {
        this.email = email;
    }

    public ForgetPassword() {
    }
}