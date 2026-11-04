package tz.co.ladha.dto.vendor_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVendorStaffDTO {
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

    /**
     * order-manager | delivery-officer | branch-admin
     */
    @NotBlank(message = "Staff role is required")
    private String role;

    /**
     * Required for branch-admin (must be vacant).
     * For order-manager / delivery-officer, defaults to the vendor's active/main branch when omitted.
     */
    private String branchId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
