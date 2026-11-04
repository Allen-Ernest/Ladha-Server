package tz.co.ladha.dto.vendor_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponseDTO {
    private String vendorId;
    private String vendorName;
    private Integer registrationNumber;
    private String vendorType;
    private String description;
    private String vendorAddress;
    private String vendorPhone;
    private String vendorEmail;
    private Boolean active;
    private Double rating;
    private String categoryId;
    private String categoryName;
    private String wardId;
    private String wardName;
    private String districtId;
    private String districtName;
    private Boolean hasAdministrator;
    private String administratorId;
    private String administratorName;
}
