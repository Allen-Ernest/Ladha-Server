package tz.co.ladha.dto.vendor_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VendorStaffResponseDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String role;
    private Boolean active;
    private String branchId;
    private String branchName;
}
