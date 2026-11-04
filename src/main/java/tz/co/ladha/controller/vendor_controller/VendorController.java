package tz.co.ladha.controller.vendor_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tz.co.ladha.dto.vendor_dto.DeleteVendorDTO;
import tz.co.ladha.dto.vendor_dto.VendorDTO;
import tz.co.ladha.dto.vendor_dto.VendorPageResponseDTO;
import tz.co.ladha.dto.vendor_dto.VendorResponseDTO;
import tz.co.ladha.service.vendor_service.VendorService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/company")
public class VendorController {
    private final VendorService vendorService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-vendor")
    public ResponseEntity<VendorResponseDTO> registerVendor(@Valid @RequestBody VendorDTO vendorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.addVendor(vendorDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update-vendor")
    public ResponseEntity<VendorResponseDTO> updateVendor(@Valid @RequestBody VendorDTO vendorDTO) {
        return ResponseEntity.ok(vendorService.updateVendor(vendorDTO));
    }

    @GetMapping("/get-vendors")
    public ResponseEntity<VendorPageResponseDTO> getVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(vendorService.getVendors(page, size, active, q));
    }

    @GetMapping("/get-vendor/{vendorId}")
    public ResponseEntity<VendorResponseDTO> getVendor(@PathVariable String vendorId) {
        return ResponseEntity.ok(vendorService.getVendor(vendorId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete-vendor")
    public ResponseEntity<Boolean> deleteVendor(@Valid @RequestBody DeleteVendorDTO deleteVendorDTO) {
        return ResponseEntity.ok(vendorService.deleteVendor(deleteVendorDTO.getVendorId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/set-vendor-active/{vendorId}")
    public ResponseEntity<VendorResponseDTO> setVendorActive(
            @PathVariable String vendorId,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(vendorService.setVendorActive(vendorId, active));
    }
}
