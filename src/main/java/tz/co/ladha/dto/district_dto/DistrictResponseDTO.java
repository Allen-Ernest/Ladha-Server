package tz.co.ladha.dto.district_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponseDTO {
    private String districtId;
    private String districtName;
    private List<WardResponseDTO> wards = new ArrayList<>();
}
