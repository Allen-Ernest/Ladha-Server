package tz.co.ladha.dto.district_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WardResponseDTO {
    private String wardId;
    private String wardName;
    private String districtId;
    private String districtName;
}
