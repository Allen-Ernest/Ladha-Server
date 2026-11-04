package tz.co.ladha.dto.vendor_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteVendorDTO {
    @NotBlank(message = "Vendor ID is required")
    private String vendorId;
}
