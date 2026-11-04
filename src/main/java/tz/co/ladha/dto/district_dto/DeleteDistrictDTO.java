package tz.co.ladha.dto.district_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteDistrictDTO {
    @NotBlank(message = "District ID is required")
    private String districtId;
}
