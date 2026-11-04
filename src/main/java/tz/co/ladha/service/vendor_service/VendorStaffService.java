package tz.co.ladha.service.vendor_service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.vendor_dto.CreateVendorStaffDTO;
import tz.co.ladha.dto.vendor_dto.VendorStaffResponseDTO;
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
import tz.co.ladha.repository.AdministratorRepository;
import tz.co.ladha.repository.UserRepository;
import tz.co.ladha.repository.user_repository.DeliveryOfficerRepository;
import tz.co.ladha.repository.user_repository.OrderManagerRepository;
import tz.co.ladha.repository.vendorRepository.BranchRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VendorStaffService {
    private final BranchService branchService;
    private final BranchRepository branchRepository;
    private final OrderManagerRepository orderManagerRepository;
    private final DeliveryOfficerRepository deliveryOfficerRepository;
    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<VendorStaffResponseDTO> listStaffForAdmin(User principal) {
        Vendor vendor = branchService.requireVendorForAdmin(principal);
        List<VendorStaffResponseDTO> rows = new ArrayList<>();

        for (OrderManager om : orderManagerRepository.findByBranch_Vendor_VendorIdOrderByFirstNameAsc(vendor.getVendorId())) {
            rows.add(toResponse(om, "order-manager", om.getBranch()));
        }
        for (DeliveryOfficer officer : deliveryOfficerRepository.findByBranch_Vendor_VendorIdOrderByFirstNameAsc(vendor.getVendorId())) {
            rows.add(toResponse(officer, "delivery-officer", officer.getBranch()));
        }
        for (Branch branch : branchRepository.findByVendorVendorId(vendor.getVendorId())) {
            Administrator admin = branch.getAdministrator();
            // Vendor-level admin on the default branch is not listed as appointed staff.
            if (admin != null && admin.getAdminType() != AdminType.VENDOR_ADMIN) {
                rows.add(toResponse(admin, "branch-admin", branch));
            }
        }

        rows.sort(Comparator
                .comparing(VendorStaffResponseDTO::getBranchName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(VendorStaffResponseDTO::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return rows;
    }

    @Transactional
    public VendorStaffResponseDTO createStaff(User principal, CreateVendorStaffDTO dto) {
        Vendor vendor = branchService.requireVendorForAdmin(principal);

        String email = requireText(dto.getEmail(), "Email").toLowerCase();
        String phone = requireText(dto.getPhone(), "Phone");
        String password = requireText(dto.getPassword(), "Password");
        String confirm = requireText(dto.getConfirmPassword(), "Password confirmation");
        if (!password.equals(confirm)) {
            throw new Exception("Passwords don't match.");
        }
        if (password.length() < 8) {
            throw new Exception("Password must be at least 8 characters long.");
        }
        assertUniqueContact(email, phone);

        String roleKey = requireText(dto.getRole(), "Staff role").toLowerCase()
                .replace('_', '-')
                .replace(' ', '-');

        return switch (roleKey) {
            case "order-manager", "ordermanager" ->
                    createOrderManager(vendor, dto, email, phone, password);
            case "delivery-officer", "deliveryofficer", "delivery-rider", "deliveryrider" ->
                    createDeliveryOfficer(vendor, dto, email, phone, password);
            case "branch-admin", "branchadmin", "vendor-branch-admin" ->
                    createBranchAdmin(vendor, dto, email, phone, password);
            default -> throw new Exception(
                    "Unsupported staff role. Use order-manager, delivery-officer, or branch-admin."
            );
        };
    }

    @Transactional
    public VendorStaffResponseDTO setStaffActive(User principal, String userId, boolean active) {
        Vendor vendor = branchService.requireVendorForAdmin(principal);
        UUID id = parseUuid(userId, "User ID");

        var omOpt = orderManagerRepository.findById(id);
        if (omOpt.isPresent()) {
            OrderManager om = omOpt.get();
            assertBranchOwned(vendor, om.getBranch());
            om.setActive(active);
            return toResponse(orderManagerRepository.save(om), "order-manager", om.getBranch());
        }

        var officerOpt = deliveryOfficerRepository.findById(id);
        if (officerOpt.isPresent()) {
            DeliveryOfficer officer = officerOpt.get();
            assertBranchOwned(vendor, officer.getBranch());
            officer.setActive(active);
            return toResponse(deliveryOfficerRepository.save(officer), "delivery-officer", officer.getBranch());
        }

        Administrator admin = administratorRepository.findById(id)
                .orElseThrow(() -> new Exception("Staff user not found."));
        Branch branch = findBranchForAdmin(vendor, admin);
        if (branch == null) {
            throw new Exception("This branch admin is not linked to your vendor.");
        }
        admin.setActive(active);
        return toResponse(administratorRepository.save(admin), "branch-admin", branch);
    }

    private VendorStaffResponseDTO createOrderManager(
            Vendor vendor,
            CreateVendorStaffDTO dto,
            String email,
            String phone,
            String password
    ) {
        Branch branch = resolveOperatingBranch(vendor, dto.getBranchId());
        OrderManager om = new OrderManager();
        applyPerson(om, dto, email, phone, password);
        om.setRole(Role.ORDER_MANAGER);
        om.setBranch(branch);
        om.setDateCreated(LocalDateTime.now());
        return toResponse(orderManagerRepository.save(om), "order-manager", branch);
    }

    private VendorStaffResponseDTO createDeliveryOfficer(
            Vendor vendor,
            CreateVendorStaffDTO dto,
            String email,
            String phone,
            String password
    ) {
        Branch branch = resolveOperatingBranch(vendor, dto.getBranchId());
        DeliveryOfficer officer = new DeliveryOfficer();
        applyPerson(officer, dto, email, phone, password);
        officer.setRole(Role.DELIVERY_OFFICER);
        officer.setBranch(branch);
        officer.setDateCreated(LocalDateTime.now());
        return toResponse(deliveryOfficerRepository.save(officer), "delivery-officer", branch);
    }

    private VendorStaffResponseDTO createBranchAdmin(
            Vendor vendor,
            CreateVendorStaffDTO dto,
            String email,
            String phone,
            String password
    ) {
        if (dto.getBranchId() == null || dto.getBranchId().isBlank()) {
            throw new Exception("Select a vacant branch for the branch admin.");
        }
        Branch branch = branchService.requireOwnedBranch(vendor, dto.getBranchId());
        if (branch.getAdministrator() != null) {
            throw new Exception("That branch already has a branch admin. Choose a vacant branch.");
        }

        long branchCount = branchRepository.countByVendorVendorId(vendor.getVendorId());
        if (branchCount <= 1) {
            throw new Exception(
                    "Branch admins can only be appointed when you have more than one branch. "
                            + "You are already the admin of your single branch."
            );
        }

        Administrator admin = new Administrator();
        applyPerson(admin, dto, email, phone, password);
        admin.setRole(Role.ADMIN);
        admin.setAdminType(AdminType.VENDOR_BRANCH_ADMIN);
        Administrator saved = administratorRepository.save(admin);

        branch.setAdministrator(saved);
        branchRepository.save(branch);
        return toResponse(saved, "branch-admin", branch);
    }

    /**
     * Order managers and delivery officers belong to the vendor admin's operating branch
     * (the branch they currently manage — typically the main branch without a dedicated BA).
     */
    private Branch resolveOperatingBranch(Vendor vendor, String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return branchService.requireOwnedBranch(vendor, branchId);
        }
        List<Branch> branches = branchRepository.findByVendorVendorId(vendor.getVendorId());
        if (branches.isEmpty()) {
            throw new Exception("No branch is available. Create a branch first.");
        }
        return branches.stream()
                .filter(b -> b.getAdministrator() == null)
                .findFirst()
                .orElse(branches.get(0));
    }

    private void applyPerson(User user, CreateVendorStaffDTO dto, String email, String phone, String password) {
        user.setFirstName(requireText(dto.getFirstName(), "First name"));
        user.setLastName(requireText(dto.getLastName(), "Last name"));
        user.setEmail(email);
        user.setPhone(phone);
        user.setGender(resolveGender(dto.getGender()));
        user.setActive(true);
        user.setPassword(encoder.encode(password));
        user.setMustChangePassword(true);
    }

    private void assertUniqueContact(String email, String phone) {
        if (userRepository.existsByEmail(email)
                || administratorRepository.existsByEmailIgnoreCase(email)
                || orderManagerRepository.existsByEmailIgnoreCase(email)
                || deliveryOfficerRepository.existsByEmailIgnoreCase(email)) {
            throw new Exception("Email address already in use.");
        }
        if (userRepository.existsByPhone(phone)
                || administratorRepository.existsByPhone(phone)
                || orderManagerRepository.existsByPhone(phone)
                || deliveryOfficerRepository.existsByPhone(phone)) {
            throw new Exception("Phone number already in use.");
        }
    }

    private void assertBranchOwned(Vendor vendor, Branch branch) {
        if (branch == null || branch.getVendor() == null
                || !vendor.getVendorId().equals(branch.getVendor().getVendorId())) {
            throw new Exception("This staff member does not belong to your vendor.");
        }
    }

    private Branch findBranchForAdmin(Vendor vendor, Administrator admin) {
        if (admin == null || admin.getUserId() == null) {
            return null;
        }
        return branchRepository.findByVendorVendorId(vendor.getVendorId()).stream()
                .filter(b -> b.getAdministrator() != null
                        && admin.getUserId().equals(b.getAdministrator().getUserId()))
                .findFirst()
                .orElse(null);
    }

    private VendorStaffResponseDTO toResponse(User user, String role, Branch branch) {
        return new VendorStaffResponseDTO(
                user.getUserId() != null ? user.getUserId().toString() : null,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender() != null ? user.getGender().name() : null,
                role,
                user.getActive(),
                branch != null && branch.getBranchId() != null ? branch.getBranchId().toString() : null,
                branch != null ? branch.getBranchName() : null
        );
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

    private UUID parseUuid(String raw, String label) {
        if (raw == null || raw.isBlank()) {
            throw new Exception(label + " is required.");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid " + label.toLowerCase() + ".");
        }
    }
}
