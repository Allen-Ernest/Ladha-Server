package tz.co.ladha.service.vendor_service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.vendor_dto.VendorDTO;
import tz.co.ladha.dto.vendor_dto.VendorPageResponseDTO;
import tz.co.ladha.dto.vendor_dto.VendorResponseDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.district.Ward;
import tz.co.ladha.model.menu.MenuCategory;
import tz.co.ladha.model.user.admin.Administrator;
import tz.co.ladha.model.vendor.Branch;
import tz.co.ladha.model.vendor.Vendor;
import tz.co.ladha.model.vendor.VendorType;
import tz.co.ladha.repository.district_repository.WardRepository;
import tz.co.ladha.repository.menu_repository.MenuCategoryRepository;
import tz.co.ladha.repository.vendorRepository.BranchRepository;
import tz.co.ladha.repository.vendorRepository.VendorRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VendorService {
    private final VendorRepository vendorRepository;
    private final BranchRepository branchRepository;
    private final BranchService branchService;
    private final WardRepository wardRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    @Transactional
    public VendorResponseDTO addVendor(VendorDTO dto) {
        int registrationNumber = requireRegistrationNumber(dto.getRegistrationNumber());
        String email = requireText(dto.getVendorEmail(), "Email").toLowerCase();
        String phone = requireText(dto.getVendorPhone(), "Phone number");

        if (vendorRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new Exception("A vendor with this registration number already exists.");
        }
        if (vendorRepository.existsByVendorEmailIgnoreCase(email)) {
            throw new Exception("A vendor with this email already exists.");
        }
        if (vendorRepository.existsByVendorPhone(phone)) {
            throw new Exception("A vendor with this phone number already exists.");
        }

        Vendor vendor = new Vendor();
        applyVendorFields(vendor, dto, registrationNumber, email, phone);
        vendor.setActive(dto.getActive() == null || Boolean.TRUE.equals(dto.getActive()));
        vendor.setRating(0);

        Vendor saved = vendorRepository.save(vendor);
        // Every vendor starts with one main branch; the vendor admin acts as its branch admin.
        branchService.ensureDefaultBranch(saved);
        return toResponse(saved);
    }

    @Transactional
    public VendorResponseDTO updateVendor(VendorDTO dto) {
        Vendor vendor = requireVendor(dto.getVendorId());
        int registrationNumber = requireRegistrationNumber(dto.getRegistrationNumber());
        String email = requireText(dto.getVendorEmail(), "Email").toLowerCase();
        String phone = requireText(dto.getVendorPhone(), "Phone number");

        if (vendor.getRegistrationNumber() != registrationNumber
                && vendorRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new Exception("A vendor with this registration number already exists.");
        }
        if (!vendor.getVendorEmail().equalsIgnoreCase(email)
                && vendorRepository.existsByVendorEmailIgnoreCase(email)) {
            throw new Exception("A vendor with this email already exists.");
        }
        if (!vendor.getVendorPhone().equals(phone)
                && vendorRepository.existsByVendorPhone(phone)) {
            throw new Exception("A vendor with this phone number already exists.");
        }

        applyVendorFields(vendor, dto, registrationNumber, email, phone);
        if (dto.getActive() != null) {
            vendor.setActive(dto.getActive());
            syncBranchActiveState(vendor.getVendorId(), dto.getActive());
        }

        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional(readOnly = true)
    public VendorResponseDTO getVendor(String vendorId) {
        return toResponse(requireVendor(vendorId));
    }

    @Transactional(readOnly = true)
    public VendorPageResponseDTO getVendors(int page, int size, Boolean active, String query) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 10 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "vendorName"));

        String q = query == null ? "" : query.trim();
        Page<Vendor> result = vendorRepository.findFiltered(active, q, pageable);

        List<VendorResponseDTO> content = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new VendorPageResponseDTO(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public Boolean deleteVendor(String vendorId) {
        Vendor vendor = requireVendor(vendorId);
        List<Branch> branches = branchRepository.findByVendorVendorId(vendor.getVendorId());
        if (!branches.isEmpty()) {
            branchRepository.deleteAll(branches);
        }
        vendorRepository.delete(vendor);
        return true;
    }

    @Transactional
    public VendorResponseDTO setVendorActive(String vendorId, boolean active) {
        Vendor vendor = requireVendor(vendorId);
        vendor.setActive(active);
        syncBranchActiveState(vendor.getVendorId(), active);
        return toResponse(vendorRepository.save(vendor));
    }

    private void applyVendorFields(
            Vendor vendor,
            VendorDTO dto,
            int registrationNumber,
            String email,
            String phone
    ) {
        vendor.setVendorName(requireText(dto.getVendorName(), "Vendor name"));
        vendor.setRegistrationNumber(registrationNumber);
        vendor.setDescription(requireText(dto.getDescription(), "Description"));
        vendor.setVendorAddress(requireText(dto.getVendorAddress(), "Vendor address"));
        vendor.setVendorEmail(email);
        vendor.setVendorPhone(phone);
        vendor.setVendorType(resolveVendorType(dto.getVendorType()));
        vendor.setPrimaryCategory(requireCategory(dto.getCategoryId()));
        vendor.setHQ(requireWard(dto.getWardId()));
    }

    private void syncBranchActiveState(UUID vendorId, boolean active) {
        List<Branch> branches = branchRepository.findByVendorVendorId(vendorId);
        for (Branch branch : branches) {
            branch.setActive(active);
            branchRepository.save(branch);
        }
    }

    private Vendor requireVendor(String vendorId) {
        if (vendorId == null || vendorId.isBlank()) {
            throw new Exception("Vendor ID is required.");
        }
        UUID id;
        try {
            id = UUID.fromString(vendorId.trim());
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid vendor ID.");
        }
        return vendorRepository.findById(id)
                .orElseThrow(() -> new Exception("Vendor not found."));
    }

    private MenuCategory requireCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new Exception("Primary cuisine category is required.");
        }
        return menuCategoryRepository.findById(categoryId.trim())
                .orElseThrow(() -> new Exception("Menu category not found."));
    }

    private Ward requireWard(String wardId) {
        if (wardId == null || wardId.isBlank()) {
            throw new Exception("Ward is required.");
        }
        UUID id;
        try {
            id = UUID.fromString(wardId.trim());
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid ward ID.");
        }
        return wardRepository.findById(id)
                .orElseThrow(() -> new Exception("Ward not found."));
    }

    private VendorType resolveVendorType(String rawType) {
        String type = requireText(rawType, "Vendor type").toLowerCase()
                .replace('_', '-')
                .replace(' ', '-');

        return switch (type) {
            case "bar" -> VendorType.BAR;
            case "cafeteria", "cafetaria" -> VendorType.CAFETERIA;
            case "hotel" -> VendorType.HOTEL;
            case "night-club", "nightclub" -> VendorType.NIGHT_CLUB;
            case "restaurant" -> VendorType.RESTAURANT;
            case "super-market", "supermarket" -> VendorType.SUPER_MARKET;
            default -> throw new Exception("Unsupported vendor type: " + rawType);
        };
    }

    private int requireRegistrationNumber(Integer registrationNumber) {
        if (registrationNumber == null || registrationNumber <= 0) {
            throw new Exception("Registration number must be a positive number.");
        }
        return registrationNumber;
    }

    private String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new Exception(label + " is required.");
        }
        return value.trim();
    }

    private VendorResponseDTO toResponse(Vendor vendor) {
        Ward ward = vendor.getHQ();
        MenuCategory category = vendor.getPrimaryCategory();
        Administrator admin = vendor.getAdministrator();
        String administratorName = null;
        String administratorId = null;
        if (admin != null) {
            administratorId = admin.getUserId() != null ? admin.getUserId().toString() : null;
            administratorName = (admin.getFirstName() + " " + admin.getLastName()).trim();
            if (administratorName.isBlank()) {
                administratorName = admin.getEmail();
            }
        }

        return new VendorResponseDTO(
                vendor.getVendorId().toString(),
                vendor.getVendorName(),
                vendor.getRegistrationNumber(),
                vendor.getVendorType().name(),
                vendor.getDescription(),
                vendor.getVendorAddress(),
                vendor.getVendorPhone(),
                vendor.getVendorEmail(),
                vendor.getActive(),
                vendor.getRating(),
                category.getCategoryId(),
                category.getCategoryName(),
                ward.getWardId().toString(),
                ward.getWardName(),
                ward.getDistrict().getDistrictId().toString(),
                ward.getDistrict().getDistrictName(),
                admin != null,
                administratorId,
                administratorName
        );
    }
}
