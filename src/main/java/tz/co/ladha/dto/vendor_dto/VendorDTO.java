package tz.co.ladha.dto.vendor_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorDTO {
    private String vendorId;

    @NotBlank(message = "Vendor name is required")
    private String vendorName;

    @NotBlank(message = "Vendor type is required")
    private String vendorType;

    @NotNull(message = "Registration number is required")
    private Integer registrationNumber;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Vendor address is required")
    private String vendorAddress;

    @NotBlank(message = "Phone number is required")
    private String vendorPhone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String vendorEmail;

    @NotBlank(message = "Primary cuisine category is required")
    private String categoryId;

    @NotBlank(message = "Ward is required")
    private String wardId;

    /** Defaults to true on create when omitted. */
    private Boolean active;
}
