package tz.co.ladha.dto.vendor_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BranchResponseDTO {
    private String branchId;
    private String branchName;
    private String location;
    private String address;
    private Boolean active;
    private String vendorId;
    private String vendorName;
    /** True when no dedicated branch admin is assigned. */
    private Boolean vacant;
    private String branchAdminId;
    private String branchAdminName;
}
