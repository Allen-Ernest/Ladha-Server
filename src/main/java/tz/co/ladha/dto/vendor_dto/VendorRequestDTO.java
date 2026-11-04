package tz.co.ladha.dto.vendor_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorRequestDTO {
    @NotBlank
    private String vendorName;

    @NotBlank
    private String vendorAddress;

    @NotBlank
    private int registrationNumber;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String email;
}