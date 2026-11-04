package tz.co.ladha.dto.vendor_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteBranchDTO {
    @NotBlank(message = "Branch ID is required")
    private String branchId;
}
