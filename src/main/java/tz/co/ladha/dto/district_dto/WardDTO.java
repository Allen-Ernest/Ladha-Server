package tz.co.ladha.dto.district_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WardDTO {
    private String wardId;

    @NotBlank(message = "District ID is required")
    private String districtId;

    @NotBlank(message = "Ward name is required")
    private String wardName;
}
