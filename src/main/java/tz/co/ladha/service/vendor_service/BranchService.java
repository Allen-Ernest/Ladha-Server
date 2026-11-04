package tz.co.ladha.service.vendor_service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.vendor_dto.BranchDTO;
import tz.co.ladha.dto.vendor_dto.BranchResponseDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.user.User;
import tz.co.ladha.model.user.admin.AdminType;
import tz.co.ladha.model.user.admin.Administrator;
import tz.co.ladha.model.vendor.Branch;
import tz.co.ladha.model.vendor.Vendor;
import tz.co.ladha.repository.menu_repository.MenuRepository;
import tz.co.ladha.repository.user_repository.DeliveryOfficerRepository;
import tz.co.ladha.repository.user_repository.OrderManagerRepository;
import tz.co.ladha.repository.vendorRepository.BranchRepository;
import tz.co.ladha.repository.vendorRepository.VendorRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;
    private final VendorRepository vendorRepository;
    private final MenuRepository menuRepository;
    private final OrderManagerRepository orderManagerRepository;
    private final DeliveryOfficerRepository deliveryOfficerRepository;

    /**
     * Branches for the vendor linked to the signed-in vendor administrator.
     * Ensures a default (main) branch exists so single-branch vendors can manage menus
     * without a separate branch-admin account.
     */
    @Transactional
    public List<BranchResponseDTO> listBranchesForAdmin(User principal) {
        Vendor vendor = requireVendorForAdmin(principal);
        ensureDefaultBranch(vendor);
        // Backfill: vendor admin must own the default branch admin slot when it is still vacant.
        if (vendor.getAdministrator() != null) {
            assignVendorAdminToDefaultBranch(vendor, vendor.getAdministrator());
        }
        return branchRepository.findByVendorVendorId(vendor.getVendorId()).stream()
                .map(branch -> toResponse(branch, vendor))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BranchResponseDTO> listVacantBranchesForAdmin(User principal) {
        Vendor vendor = requireVendorForAdmin(principal);
        return branchRepository.findByVendorVendorIdAndAdministratorIsNull(vendor.getVendorId()).stream()
                .map(branch -> toResponse(branch, vendor))
                .toList();
    }

    @Transactional(readOnly = true)
    public BranchResponseDTO getBranchForAdmin(User principal, String branchId) {
        Vendor vendor = requireVendorForAdmin(principal);
        Branch branch = requireOwnedBranch(vendor, branchId);
        return toResponse(branch, vendor);
    }

    /**
     * Vendor admin creates an additional branch. The vendor admin remains the operator
     * of every branch until dedicated branch admins are appointed later.
     */
    @Transactional
    public BranchResponseDTO createBranchForAdmin(User principal, BranchDTO dto) {
        Vendor vendor = requireVendorForAdmin(principal);
        ensureDefaultBranch(vendor);

        String name = requireText(dto.getBranchName(), "Branch name");
        String location = requireText(dto.getLocation(), "Location");
        String address = dto.getAddress() != null && !dto.getAddress().isBlank()
                ? dto.getAddress().trim()
                : defaultBranchAddress(vendor);

        boolean duplicate = branchRepository.findByVendorVendorId(vendor.getVendorId()).stream()
                .anyMatch(b -> b.getBranchName() != null && b.getBranchName().equalsIgnoreCase(name));
        if (duplicate) {
            throw new Exception("A branch with this name already exists for your vendor.");
        }

        Branch branch = new Branch();
        branch.setVendor(vendor);
        branch.setBranchName(name);
        branch.setLocation(location);
        branch.setAddress(address);
        branch.setActive(dto.getActive() == null || Boolean.TRUE.equals(dto.getActive()));

        return toResponse(branchRepository.save(branch), vendor);
    }

    @Transactional
    public BranchResponseDTO updateBranchForAdmin(User principal, BranchDTO dto) {
        Vendor vendor = requireVendorForAdmin(principal);
        Branch branch = requireOwnedBranch(vendor, dto.getBranchId());

        String name = requireText(dto.getBranchName(), "Branch name");
        String location = requireText(dto.getLocation(), "Location");
        String address = requireText(
                dto.getAddress() != null && !dto.getAddress().isBlank()
                        ? dto.getAddress()
                        : branch.getAddress(),
                "Address"
        );

        boolean duplicate = branchRepository.findByVendorVendorId(vendor.getVendorId()).stream()
                .anyMatch(b ->
                        b.getBranchId() != null
                                && !b.getBranchId().equals(branch.getBranchId())
                                && b.getBranchName() != null
                                && b.getBranchName().equalsIgnoreCase(name)
                );
        if (duplicate) {
            throw new Exception("A branch with this name already exists for your vendor.");
        }

        branch.setBranchName(name);
        branch.setLocation(location);
        branch.setAddress(address);
        if (dto.getActive() != null) {
            branch.setActive(dto.getActive());
        }

        return toResponse(branchRepository.save(branch), vendor);
    }

    @Transactional
    public Boolean deleteBranchForAdmin(User principal, String branchId) {
        Vendor vendor = requireVendorForAdmin(principal);
        Branch branch = requireOwnedBranch(vendor, branchId);

        long total = branchRepository.countByVendorVendorId(vendor.getVendorId());
        if (total <= 1) {
            throw new Exception("You cannot delete your only branch.");
        }

        if (branch.getAdministrator() != null) {
            throw new Exception("Remove or reassign the branch admin before deleting this branch.");
        }

        if (menuRepository.countByBranch_BranchId(branch.getBranchId()) > 0) {
            throw new Exception("This branch still has menu items. Remove them before deleting the branch.");
        }
        if (orderManagerRepository.countByBranch_BranchId(branch.getBranchId()) > 0) {
            throw new Exception("This branch still has order managers. Remove them before deleting the branch.");
        }
        if (deliveryOfficerRepository.countByBranch_BranchId(branch.getBranchId()) > 0) {
            throw new Exception("This branch still has delivery officers. Remove them before deleting the branch.");
        }

        branchRepository.delete(branch);
        return true;
    }

    /**
     * Creates the main branch for a newly registered vendor (or returns the existing one).
     * Naming: "{vendorName} Main"; address defaults to the vendor's address.
     */
    @Transactional
    public Branch ensureDefaultBranch(Vendor vendor) {
        if (vendor == null || vendor.getVendorId() == null) {
            throw new Exception("Vendor is required to create a default branch.");
        }

        List<Branch> existing = branchRepository.findByVendorVendorId(vendor.getVendorId());
        if (!existing.isEmpty()) {
            return resolveDefaultBranch(existing, vendor);
        }

        Branch branch = new Branch();
        branch.setVendor(vendor);
        branch.setBranchName(defaultBranchName(vendor));
        branch.setAddress(defaultBranchAddress(vendor));
        branch.setLocation(defaultBranchLocation(vendor));
        branch.setActive(vendor.getActive() == null || Boolean.TRUE.equals(vendor.getActive()));
        if (vendor.getAdministrator() != null) {
            branch.setAdministrator(vendor.getAdministrator());
        }
        return branchRepository.save(branch);
    }

    /**
     * Assigns the vendor administrator to the default (main) branch when that slot is vacant
     * or still held by a previous vendor-level admin. Does not overwrite a dedicated branch admin.
     */
    @Transactional
    public Branch assignVendorAdminToDefaultBranch(Vendor vendor, Administrator vendorAdmin) {
        if (vendor == null || vendor.getVendorId() == null) {
            throw new Exception("Vendor is required.");
        }
        if (vendorAdmin == null) {
            throw new Exception("Vendor administrator is required.");
        }

        Branch main = ensureDefaultBranch(vendor);
        Administrator current = main.getAdministrator();

        if (current != null
                && current.getUserId() != null
                && current.getUserId().equals(vendorAdmin.getUserId())) {
            return main;
        }

        // Keep dedicated VENDOR_BRANCH_ADMIN appointments on the default branch if somehow set.
        if (current != null
                && current.getAdminType() == AdminType.VENDOR_BRANCH_ADMIN
                && (current.getUserId() == null || !current.getUserId().equals(vendorAdmin.getUserId()))) {
            return main;
        }

        main.setAdministrator(vendorAdmin);
        return branchRepository.save(main);
    }

    /** Clears branch.administrator where it matches the given admin (e.g. on vendor-admin unlink). */
    @Transactional
    public void clearAdministratorFromBranches(Vendor vendor, UUID administratorId) {
        if (vendor == null || vendor.getVendorId() == null || administratorId == null) {
            return;
        }
        for (Branch branch : branchRepository.findByVendorVendorId(vendor.getVendorId())) {
            Administrator admin = branch.getAdministrator();
            if (admin != null && administratorId.equals(admin.getUserId())) {
                branch.setAdministrator(null);
                branchRepository.save(branch);
            }
        }
    }

    public Branch requireBranch(UUID branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new Exception("Branch not found."));
    }

    private Branch resolveDefaultBranch(List<Branch> branches, Vendor vendor) {
        String expectedMain = defaultBranchName(vendor);
        return branches.stream()
                .filter(b -> b.getBranchName() != null && b.getBranchName().equalsIgnoreCase(expectedMain))
                .findFirst()
                .orElseGet(() -> branches.stream()
                        .filter(b -> b.getBranchName() != null && b.getBranchName().toLowerCase().endsWith(" main"))
                        .findFirst()
                        .orElse(branches.get(0)));
    }

    public Vendor requireVendorForAdmin(User principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new Exception("Authenticated admin not found.");
        }

        return vendorRepository.findByAdministrator_UserId(principal.getUserId())
                .orElseThrow(() -> new Exception(
                        "No vendor is linked to this administrator account."
                ));
    }

    public Branch requireOwnedBranch(Vendor vendor, String branchId) {
        UUID id = parseUuid(branchId, "Branch ID");
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new Exception("Branch not found."));
        if (branch.getVendor() == null
                || branch.getVendor().getVendorId() == null
                || !branch.getVendor().getVendorId().equals(vendor.getVendorId())) {
            throw new Exception("This branch does not belong to your vendor.");
        }
        return branch;
    }

    private String defaultBranchName(Vendor vendor) {
        String base = vendor.getVendorName() != null ? vendor.getVendorName().trim() : "Vendor";
        return base + " Main";
    }

    /** Branch address defaults to the vendor's registered address. */
    private String defaultBranchAddress(Vendor vendor) {
        if (vendor.getVendorAddress() != null && !vendor.getVendorAddress().isBlank()) {
            return vendor.getVendorAddress().trim();
        }
        if (vendor.getHQ() != null && vendor.getHQ().getWardName() != null) {
            return vendor.getHQ().getWardName().trim();
        }
        throw new Exception("Vendor address is required to create the default branch.");
    }

    private String defaultBranchLocation(Vendor vendor) {
        if (vendor.getHQ() != null && vendor.getHQ().getWardName() != null
                && !vendor.getHQ().getWardName().isBlank()) {
            return vendor.getHQ().getWardName().trim();
        }
        return defaultBranchAddress(vendor);
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

    private BranchResponseDTO toResponse(Branch branch, Vendor vendor) {
        Administrator admin = branch.getAdministrator();
        String adminId = null;
        String adminName = null;
        if (admin != null) {
            adminId = admin.getUserId() != null ? admin.getUserId().toString() : null;
            adminName = ((admin.getFirstName() != null ? admin.getFirstName() : "")
                    + " "
                    + (admin.getLastName() != null ? admin.getLastName() : "")).trim();
            if (adminName.isBlank()) {
                adminName = admin.getEmail();
            }
        }

        return new BranchResponseDTO(
                branch.getBranchId() != null ? branch.getBranchId().toString() : null,
                branch.getBranchName(),
                branch.getLocation(),
                branch.getAddress(),
                branch.getActive(),
                vendor.getVendorId() != null ? vendor.getVendorId().toString() : null,
                vendor.getVendorName(),
                admin == null,
                adminId,
                adminName
        );
    }
}
