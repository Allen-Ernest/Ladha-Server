package tz.co.ladha.dto.user_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAdminDTO {
    @NotBlank(message = "User ID is required")
    private String userId;
}
