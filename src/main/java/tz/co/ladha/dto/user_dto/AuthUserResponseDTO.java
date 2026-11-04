package tz.co.ladha.dto.user_dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUserResponseDTO {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String gender;
    private String role;
    private Boolean active;
    private Boolean mustChangePassword;
    /** Present for Administrator accounts. */
    private String adminType;
    /** Present for OrderManager / DeliveryOfficer (and optionally branch admins). */
    private String branchId;
    private String branchName;
    private String vendorId;
    private String vendorName;
}
