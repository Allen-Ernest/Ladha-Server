package tz.co.ladha.controller.auth_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tz.co.ladha.dto.user_dto.*;
import tz.co.ladha.model.user.User;
import tz.co.ladha.service.auth_service.AdminUserService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping("/get-admins")
    public ResponseEntity<AdminUserPageResponseDTO> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String adminType,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(adminUserService.getAdmins(page, size, active, adminType, q));
    }

    @GetMapping("/get-admin/{userId}")
    public ResponseEntity<AdminUserResponseDTO> getAdmin(@PathVariable String userId) {
        return ResponseEntity.ok(adminUserService.getAdmin(userId));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<AdminUserResponseDTO> createAdmin(@Valid @RequestBody CreateAdminDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createAdmin(dto));
    }

    @PostMapping("/update-admin")
    public ResponseEntity<AdminUserResponseDTO> updateAdmin(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody UpdateAdminDTO dto
    ) {
        return ResponseEntity.ok(adminUserService.updateAdmin(principal, dto));
    }

    @PostMapping("/delete-admin")
    public ResponseEntity<Boolean> deleteAdmin(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody DeleteAdminDTO dto
    ) {
        return ResponseEntity.ok(adminUserService.deleteAdmin(principal, dto.getUserId()));
    }

    @PostMapping("/set-admin-active/{userId}")
    public ResponseEntity<AdminUserResponseDTO> setAdminActive(
            @AuthenticationPrincipal User principal,
            @PathVariable String userId,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(adminUserService.setAdminActive(principal, userId, active));
    }
}
