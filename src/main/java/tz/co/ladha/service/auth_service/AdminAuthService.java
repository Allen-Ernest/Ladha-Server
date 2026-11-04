package tz.co.ladha.service.auth_service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.AuthResponse;
import tz.co.ladha.dto.user_dto.AdminProfileUpdateDTO;
import tz.co.ladha.dto.user_dto.AuthUserResponseDTO;
import tz.co.ladha.dto.user_dto.UserDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.user.Gender;
import tz.co.ladha.model.user.Role;
import tz.co.ladha.model.user.User;
import tz.co.ladha.model.user.admin.AdminType;
import tz.co.ladha.model.user.admin.Administrator;
import tz.co.ladha.model.user.delivery.DeliveryOfficer;
import tz.co.ladha.model.user.order_manager.OrderManager;
import tz.co.ladha.model.vendor.Branch;
import tz.co.ladha.model.vendor.Vendor;
import tz.co.ladha.repository.UserRepository;
import tz.co.ladha.service.JWTService;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminAuthService {
    @Autowired
    private JWTService jwtService;
    private UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public User registerAdmin(UserDTO userDTO) {
        String email = userDTO.getEmail().trim();
        String phone = userDTO.getPhone().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email address already in use.");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new Exception("Phone number already in use.");
        }

        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new Exception("Passwords don't match.");
        }

        Administrator administrator = new Administrator();
        administrator.setEmail(email);
        administrator.setPhone(phone);
        administrator.setFirstName(userDTO.getFirstName().trim());
        administrator.setLastName(userDTO.getLastName().trim());
        administrator.setActive(true);
        administrator.setRole(Role.ADMIN);

        switch (userDTO.getAdminType()) {
            case "restaurant-admin":
            case "vendor-admin":
                administrator.setAdminType(AdminType.VENDOR_ADMIN);
                break;

            case "restaurant-branch-admin":
            case "vendor-branch-admin":
                administrator.setAdminType(AdminType.VENDOR_BRANCH_ADMIN);
                break;

            case "system-admin":
                administrator.setAdminType(AdminType.SYSTEM_ADMIN);
                break;

            case "main-admin":
                administrator.setAdminType(AdminType.MAIN_ADMIN);
                break;
        }

        if (userDTO.getGender().trim().equalsIgnoreCase("male")) {
            administrator.setGender(Gender.MALE);
        } else {
            administrator.setGender(Gender.FEMALE);
        }
        administrator.setPassword(encoder.encode(userDTO.getPassword()));
        administrator.setMustChangePassword(false);
        return userRepository.save(administrator);
    }

    @Transactional(readOnly = true)
    public Optional<AuthResponse> loginAdmin(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && encoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();

            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new Exception("Account has been disabled.");
            }

            if (user.getMustChangePassword() == null) {
                user.setMustChangePassword(false);
            }

            String token = jwtService.generateToken(user);
            return Optional.of(new AuthResponse(token, toAuthUser(user)));
        }
        return Optional.empty();
    }

    @Transactional
    public AuthUserResponseDTO changePassword(User principal, String currentPassword, String newPassword, String confirmPassword) {
        User user = requireCurrentUser(principal);

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Current password is required.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new Exception("New password is required.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new Exception("Passwords don't match.");
        }
        if (newPassword.length() < 8) {
            throw new Exception("New password must be at least 8 characters long.");
        }
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect.");
        }
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new Exception("New password must be different from the current password.");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setMustChangePassword(false);
        return toAuthUser(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthUserResponseDTO getAdminProfile(User principal) {
        return toAuthUser(requireCurrentUser(principal));
    }

    @Transactional
    public AuthUserResponseDTO updateAdminProfile(User principal, AdminProfileUpdateDTO dto) {
        User user = requireCurrentUser(principal);

        String email = dto.getEmail().trim();
        String phone = dto.getPhone().trim();

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new Exception("Email address already in use.");
        }

        if (!user.getPhone().equals(phone) && userRepository.existsByPhone(phone)) {
            throw new Exception("Phone number already in use.");
        }

        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(email);
        user.setPhone(phone);

        if (dto.getGender().trim().equalsIgnoreCase("male")) {
            user.setGender(Gender.MALE);
        } else if (dto.getGender().trim().equalsIgnoreCase("female")) {
            user.setGender(Gender.FEMALE);
        } else {
            throw new Exception("Gender must be male or female.");
        }

        return toAuthUser(userRepository.save(user));
    }

    @Transactional
    public AuthUserResponseDTO disableAdminAccount(User principal, String password) {
        User user = requireCurrentUser(principal);

        if (password == null || password.isBlank()) {
            throw new Exception("Password is required to disable your account.");
        }

        if (!encoder.matches(password, user.getPassword())) {
            throw new Exception("Incorrect password. Account was not disabled.");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new Exception("Account is already disabled.");
        }

        user.setActive(false);
        return toAuthUser(userRepository.save(user));
    }

    public AuthUserResponseDTO toAuthUser(User user) {
        AuthUserResponseDTO.AuthUserResponseDTOBuilder builder = AuthUserResponseDTO.builder()
                .userId(user.getUserId() != null ? user.getUserId().toString() : null)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.getActive())
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()));

        if (user instanceof Administrator admin) {
            builder.adminType(admin.getAdminType() != null ? admin.getAdminType().name() : null);
        }

        if (user instanceof OrderManager om) {
            applyBranch(builder, om.getBranch());
        } else if (user instanceof DeliveryOfficer officer) {
            applyBranch(builder, officer.getBranch());
        }

        return builder.build();
    }

    private void applyBranch(AuthUserResponseDTO.AuthUserResponseDTOBuilder builder, Branch branch) {
        if (branch == null) {
            return;
        }
        builder.branchId(branch.getBranchId() != null ? branch.getBranchId().toString() : null);
        builder.branchName(branch.getBranchName());
        Vendor vendor = branch.getVendor();
        if (vendor != null) {
            builder.vendorId(vendor.getVendorId() != null ? vendor.getVendorId().toString() : null);
            builder.vendorName(vendor.getVendorName());
        }
    }

    private User requireCurrentUser(User principal) {
        if (principal == null || principal.getEmail() == null) {
            throw new Exception("Authenticated user not found.");
        }

        return userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new Exception("User profile not found."));
    }
}
