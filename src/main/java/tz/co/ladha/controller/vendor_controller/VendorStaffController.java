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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tz.co.ladha.dto.vendor_dto.CreateVendorStaffDTO;
import tz.co.ladha.dto.vendor_dto.VendorStaffResponseDTO;
import tz.co.ladha.model.user.User;
import tz.co.ladha.service.vendor_service.VendorStaffService;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/vendor-staff")
public class VendorStaffController {
    private final VendorStaffService vendorStaffService;

    @GetMapping
    public ResponseEntity<List<VendorStaffResponseDTO>> list(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(vendorStaffService.listStaffForAdmin(principal));
    }

    @PostMapping("/add")
    public ResponseEntity<VendorStaffResponseDTO> add(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody CreateVendorStaffDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorStaffService.createStaff(principal, dto));
    }

    @PostMapping("/{userId}/active")
    public ResponseEntity<VendorStaffResponseDTO> setActive(
            @AuthenticationPrincipal User principal,
            @PathVariable String userId,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(vendorStaffService.setStaffActive(principal, userId, active));
    }
}
