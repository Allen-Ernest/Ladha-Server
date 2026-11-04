package tz.co.ladha.dto.district_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistrictDTO {
    private String districtId;

    @NotBlank(message = "District name is required")
    private String districtName;
}
