package tz.co.ladha.controller.auth_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tz.co.ladha.dto.AuthResponse;
import tz.co.ladha.dto.user_dto.AdminProfileUpdateDTO;
import tz.co.ladha.dto.user_dto.AuthUserResponseDTO;
import tz.co.ladha.dto.user_dto.ChangePasswordDTO;
import tz.co.ladha.dto.user_dto.DisableAdminDTO;
import tz.co.ladha.dto.user_dto.UserDTO;
import tz.co.ladha.model.user.User;
import tz.co.ladha.service.auth_service.AdminAuthService;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AdminAuthController {
    private AdminAuthService adminAuthService;

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody UserDTO userDTO) {
        User createdUser = adminAuthService.registerAdmin(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAuthService.toAuthUser(createdUser));
    }

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody UserDTO userDTO) {
        Optional<AuthResponse> response = adminAuthService.loginAdmin(userDTO.getEmail(), userDTO.getPassword());

        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/admin/me")
    public ResponseEntity<AuthUserResponseDTO> getCurrentAdmin(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminAuthService.getAdminProfile(user));
    }

    @PostMapping("/update-admin-profile")
    public ResponseEntity<?> updateAdminProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AdminProfileUpdateDTO profileUpdateDTO
    ) {
        return ResponseEntity.ok(adminAuthService.updateAdminProfile(user, profileUpdateDTO));
    }

    @PostMapping("/disable-admin-account")
    public ResponseEntity<?> disableAdminAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DisableAdminDTO disableAdminDTO
    ) {
        return ResponseEntity.ok(adminAuthService.disableAdminAccount(user, disableAdminDTO.getPassword()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO
    ) {
        return ResponseEntity.ok(adminAuthService.changePassword(
                user,
                changePasswordDTO.getCurrentPassword(),
                changePasswordDTO.getNewPassword(),
                changePasswordDTO.getConfirmPassword()
        ));
    }
}
