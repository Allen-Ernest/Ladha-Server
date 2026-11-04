package tz.co.ladha.dto.user_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminDTO {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Admin type is required")
    private String adminType;

    private Boolean active;

    /** Required when adminType is vendor-admin / restaurant-admin. */
    private String vendorId;

    private Boolean replaceVendorAdmin;

    /** Optional on update — leave blank to keep the current password. */
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    private String confirmPassword;
}
