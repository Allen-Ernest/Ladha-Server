package tz.co.ladha.dto.vendor_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchDTO {
    private String branchId;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Location is required")
    private String location;

    /** Defaults to the vendor address when omitted on create. */
    private String address;

    /** Defaults to true when omitted. */
    private Boolean active;
}
