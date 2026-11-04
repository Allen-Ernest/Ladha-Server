package tz.co.ladha.dto.district_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteWardDTO {
    @NotBlank(message = "Ward ID is required")
    private String wardId;
}
