package tz.co.ladha.service.auth_service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.user_dto.AdminUserPageResponseDTO;
import tz.co.ladha.dto.user_dto.AdminUserResponseDTO;
import tz.co.ladha.dto.user_dto.CreateAdminDTO;
import tz.co.ladha.dto.user_dto.UpdateAdminDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.user.Gender;
import tz.co.ladha.model.user.Role;
import tz.co.ladha.model.user.User;
import tz.co.ladha.model.user.admin.AdminType;
import tz.co.ladha.model.user.admin.Administrator;
import tz.co.ladha.model.vendor.Vendor;
import tz.co.ladha.repository.AdministratorRepository;
import tz.co.ladha.repository.UserRepository;
import tz.co.ladha.repository.vendorRepository.VendorRepository;
import tz.co.ladha.service.vendor_service.BranchService;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AdminUserService {
    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final BranchService branchService;
    private final BCryptPasswordEncoder encoder;

    @Transactional(readOnly = true)
    public AdminUserPageResponseDTO getAdmins(int page, int size, Boolean active, String adminType, String query) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 10 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "firstName").and(Sort.by(Sort.Direction.ASC, "lastName"))
        );

        AdminType typeFilter = null;
        if (adminType != null && !adminType.isBlank()) {
            typeFilter = resolveAdminType(adminType);
        }

        String q = query == null ? "" : query.trim();
        Page<Administrator> result = administratorRepository.findFiltered(active, typeFilter, q, pageable);

        List<AdminUserResponseDTO> content = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new AdminUserPageResponseDTO(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AdminUserResponseDTO getAdmin(String userId) {
        return toResponse(requireAdmin(userId));
    }

    @Transactional
    public AdminUserResponseDTO createAdmin(CreateAdminDTO dto) {
        String email = requireText(dto.getEmail(), "Email").toLowerCase();
        String phone = requireText(dto.getPhone(), "Phone");
        String password = requireText(dto.getPassword(), "Password");
        String confirmPassword = requireText(dto.getConfirmPassword(), "Password confirmation");

        if (!password.equals(confirmPassword)) {
            throw new Exception("Passwords don't match.");
        }
        if (password.length() < 8) {
            throw new Exception("Password must be at least 8 characters long.");
        }

        if (administratorRepository.existsByEmailIgnoreCase(email) || userRepository.existsByEmail(email)) {
            throw new Exception("Email address already in use.");
        }
        if (administratorRepository.existsByPhone(phone) || userRepository.existsByPhone(phone)) {
            throw new Exception("Phone number already in use.");
        }

        AdminType adminType = resolveAdminType(dto.getAdminType());
        Vendor vendor = null;
        if (adminType == AdminType.VENDOR_ADMIN) {
            vendor = resolveVendorAssignment(null, dto.getVendorId(), Boolean.TRUE.equals(dto.getReplaceVendorAdmin()));
        }

        Administrator administrator = new Administrator();
        administrator.setFirstName(requireText(dto.getFirstName(), "First name"));
        administrator.setLastName(requireText(dto.getLastName(), "Last name"));
        administrator.setEmail(email);
        administrator.setPhone(phone);
        administrator.setGender(resolveGender(dto.getGender()));
        administrator.setAdminType(adminType);
        administrator.setRole(Role.ADMIN);
        administrator.setActive(true);
        administrator.setPassword(encoder.encode(password));
        administrator.setMustChangePassword(true);

        Administrator saved = administratorRepository.save(administrator);
        if (vendor != null) {
            assignVendorAdmin(vendor, saved, Boolean.TRUE.equals(dto.getReplaceVendorAdmin()));
        }

        return toResponse(saved);
    }

    @Transactional
    public AdminUserResponseDTO updateAdmin(User principal, UpdateAdminDTO dto) {
        Administrator administrator = requireAdmin(dto.getUserId());

        String email = requireText(dto.getEmail(), "Email").toLowerCase();
        String phone = requireText(dto.getPhone(), "Phone");

        if (!administrator.getEmail().equalsIgnoreCase(email)
                && (administratorRepository.existsByEmailIgnoreCase(email) || userRepository.existsByEmail(email))) {
            throw new Exception("Email address already in use.");
        }
        if (!administrator.getPhone().equals(phone)
                && (administratorRepository.existsByPhone(phone) || userRepository.existsByPhone(phone))) {
            throw new Exception("Phone number already in use.");
        }

        AdminType adminType = resolveAdminType(dto.getAdminType());
        administrator.setFirstName(requireText(dto.getFirstName(), "First name"));
        administrator.setLastName(requireText(dto.getLastName(), "Last name"));
        administrator.setEmail(email);
        administrator.setPhone(phone);
        administrator.setGender(resolveGender(dto.getGender()));
        administrator.setAdminType(adminType);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String password = dto.getPassword().trim();
            String confirm = dto.getConfirmPassword() == null ? "" : dto.getConfirmPassword().trim();
            if (!password.equals(confirm)) {
                throw new Exception("Passwords don't match.");
            }
            if (password.length() < 8) {
                throw new Exception("Password must be at least 8 characters long.");
            }
            administrator.setPassword(encoder.encode(password));
            administrator.setMustChangePassword(true);
        }

        if (dto.getActive() != null) {
            if (!Boolean.TRUE.equals(dto.getActive()) && isSameUser(principal, administrator)) {
                throw new Exception("You cannot deactivate your own account from User Management.");
            }
            administrator.setActive(dto.getActive());
        }

        Administrator saved = administratorRepository.save(administrator);

        if (adminType == AdminType.VENDOR_ADMIN) {
            Vendor vendor = resolveVendorAssignment(
                    saved.getUserId(),
                    dto.getVendorId(),
                    Boolean.TRUE.equals(dto.getReplaceVendorAdmin())
            );
            assignVendorAdmin(vendor, saved, Boolean.TRUE.equals(dto.getReplaceVendorAdmin()));
        } else {
            unlinkVendorAdmin(saved.getUserId());
        }

        return toResponse(saved);
    }

    @Transactional
    public Boolean deleteAdmin(User principal, String userId) {
        Administrator administrator = requireAdmin(userId);
        if (isSameUser(principal, administrator)) {
            throw new Exception("You cannot delete your own account.");
        }
        unlinkVendorAdmin(administrator.getUserId());
        administratorRepository.delete(administrator);
        return true;
    }

    @Transactional
    public AdminUserResponseDTO setAdminActive(User principal, String userId, boolean active) {
        Administrator administrator = requireAdmin(userId);
        if (!active && isSameUser(principal, administrator)) {
            throw new Exception("You cannot deactivate your own account from User Management.");
        }
        administrator.setActive(active);
        return toResponse(administratorRepository.save(administrator));
    }

    private Vendor resolveVendorAssignment(UUID currentAdminId, String vendorId, boolean replaceConfirmed) {
        if (vendorId == null || vendorId.isBlank()) {
            throw new Exception("Vendor is required for a vendor administrator.");
        }

        UUID id;
        try {
            id = UUID.fromString(vendorId.trim());
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid vendor ID.");
        }

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new Exception("Vendor not found."));

        Administrator existing = vendor.getAdministrator();
        if (existing != null
                && (currentAdminId == null || !existing.getUserId().equals(currentAdminId))
                && !replaceConfirmed) {
            String existingName = (existing.getFirstName() + " " + existing.getLastName()).trim();
            if (existingName.isBlank()) {
                existingName = existing.getEmail();
            }
            throw new Exception(
                    "Vendor \"" + vendor.getVendorName() + "\" already has an administrator ("
                            + existingName + "). Confirm replacement to continue."
            );
        }

        return vendor;
    }

    private void assignVendorAdmin(Vendor vendor, Administrator administrator, boolean replaceConfirmed) {
        Administrator existing = vendor.getAdministrator();
        if (existing != null && !existing.getUserId().equals(administrator.getUserId())) {
            if (!replaceConfirmed) {
                throw new Exception(
                        "Vendor \"" + vendor.getVendorName()
                                + "\" already has an administrator. Confirm replacement to continue."
                );
            }
            vendor.setAdministrator(null);
            vendorRepository.save(vendor);
        }

        unlinkVendorAdmin(administrator.getUserId());
        vendor.setAdministrator(administrator);
        vendorRepository.save(vendor);
        // Default branch is operated by the vendor admin — keep Branch.administrator in sync.
        branchService.assignVendorAdminToDefaultBranch(vendor, administrator);
    }

    private void unlinkVendorAdmin(UUID administratorId) {
        vendorRepository.findByAdministrator_UserId(administratorId).ifPresent(vendor -> {
            vendor.setAdministrator(null);
            vendorRepository.save(vendor);
            branchService.clearAdministratorFromBranches(vendor, administratorId);
        });
    }

    private Administrator requireAdmin(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new Exception("User ID is required.");
        }
        UUID id;
        try {
            id = UUID.fromString(userId.trim());
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid user ID.");
        }
        return administratorRepository.findById(id)
                .orElseThrow(() -> new Exception("Administrator not found."));
    }

    private boolean isSameUser(User principal, Administrator target) {
        if (principal == null || principal.getUserId() == null || target.getUserId() == null) {
            return false;
        }
        return principal.getUserId().equals(target.getUserId());
    }

    private AdminType resolveAdminType(String raw) {
        String type = requireText(raw, "Admin type").toLowerCase()
                .replace('_', '-')
                .replace(' ', '-');

        return switch (type) {
            case "system-admin", "systemadmin" -> AdminType.SYSTEM_ADMIN;
            case "vendor-admin", "vendoradmin", "restaurant-admin", "restaurantadmin" -> AdminType.VENDOR_ADMIN;
            case "vendor-branch-admin", "vendorbranchadmin", "restaurant-branch-admin", "restaurantbranchadmin", "branch-admin" -> AdminType.VENDOR_BRANCH_ADMIN;
            case "main-admin", "mainadmin" -> AdminType.MAIN_ADMIN;
            default -> throw new Exception("Unsupported admin type. Choose System Administrator or Vendor Administrator.");
        };
    }

    private Gender resolveGender(String raw) {
        String gender = requireText(raw, "Gender").trim().toLowerCase();
        if (gender.equals("male")) {
            return Gender.MALE;
        }
        if (gender.equals("female")) {
            return Gender.FEMALE;
        }
        throw new Exception("Gender must be male or female.");
    }

    private String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new Exception(label + " is required.");
        }
        return value.trim();
    }

    private AdminUserResponseDTO toResponse(Administrator admin) {
        Vendor linkedVendor = vendorRepository.findByAdministrator_UserId(admin.getUserId()).orElse(null);

        return new AdminUserResponseDTO(
                admin.getUserId().toString(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),
                admin.getPhone(),
                admin.getGender() != null ? admin.getGender().name() : null,
                admin.getRole() != null ? admin.getRole().name() : Role.ADMIN.name(),
                admin.getAdminType() != null ? admin.getAdminType().name() : null,
                admin.getActive(),
                linkedVendor != null ? linkedVendor.getVendorId().toString() : null,
                linkedVendor != null ? linkedVendor.getVendorName() : null
        );
    }
}
