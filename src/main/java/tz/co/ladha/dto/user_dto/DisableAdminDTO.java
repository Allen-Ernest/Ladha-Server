package tz.co.ladha.dto.user_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisableAdminDTO {
    @NotBlank(message = "Password is required to disable your account")
    private String password;
}
