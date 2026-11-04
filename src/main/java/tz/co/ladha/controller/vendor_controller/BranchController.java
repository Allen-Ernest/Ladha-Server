package tz.co.ladha.controller.vendor_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tz.co.ladha.dto.vendor_dto.BranchDTO;
import tz.co.ladha.dto.vendor_dto.BranchResponseDTO;
import tz.co.ladha.dto.vendor_dto.DeleteBranchDTO;
import tz.co.ladha.model.user.User;
import tz.co.ladha.service.vendor_service.BranchService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branchService;

    /** Branches for the vendor linked to the signed-in vendor administrator. */
    @GetMapping("/mine")
    public ResponseEntity<List<BranchResponseDTO>> myBranches(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(branchService.listBranchesForAdmin(principal));
    }

    /** Branches with no dedicated branch admin (available for appointment). */
    @GetMapping("/vacant")
    public ResponseEntity<List<BranchResponseDTO>> vacantBranches(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(branchService.listVacantBranchesForAdmin(principal));
    }

    @GetMapping("/{branchId}")
    public ResponseEntity<BranchResponseDTO> getBranch(
            @AuthenticationPrincipal User principal,
            @PathVariable String branchId
    ) {
        return ResponseEntity.ok(branchService.getBranchForAdmin(principal, branchId));
    }

    /** Vendor admin opens an additional branch under their vendor. */
    @PostMapping("/add")
    public ResponseEntity<BranchResponseDTO> addBranch(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody BranchDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranchForAdmin(principal, dto));
    }

    @PostMapping("/update")
    public ResponseEntity<BranchResponseDTO> updateBranch(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody BranchDTO dto
    ) {
        return ResponseEntity.ok(branchService.updateBranchForAdmin(principal, dto));
    }

    @PostMapping("/delete")
    public ResponseEntity<Boolean> deleteBranch(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody DeleteBranchDTO dto
    ) {
        return ResponseEntity.ok(branchService.deleteBranchForAdmin(principal, dto.getBranchId()));
    }
}
