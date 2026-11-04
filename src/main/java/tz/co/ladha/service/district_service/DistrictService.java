package tz.co.ladha.service.district_service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.district_dto.DistrictDTO;
import tz.co.ladha.dto.district_dto.DistrictResponseDTO;
import tz.co.ladha.dto.district_dto.WardDTO;
import tz.co.ladha.dto.district_dto.WardResponseDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.district.District;
import tz.co.ladha.model.district.Ward;
import tz.co.ladha.repository.district_repository.DistrictRepository;
import tz.co.ladha.repository.district_repository.WardRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DistrictService {
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    @Transactional(readOnly = true)
    public List<DistrictResponseDTO> getAllDistricts() {
        return districtRepository.findAll().stream()
                .sorted(Comparator.comparing(District::getDistrictName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDistrictResponse)
                .toList();
    }

    @Transactional
    public DistrictResponseDTO addDistrict(DistrictDTO dto) {
        String name = requireName(dto.getDistrictName(), "District name");

        if (districtRepository.existsByDistrictNameIgnoreCase(name)) {
            throw new Exception("A district named \"" + name + "\" already exists.");
        }

        District district = new District();
        district.setDistrictName(name);
        return toDistrictResponse(districtRepository.save(district));
    }

    @Transactional
    public DistrictResponseDTO updateDistrict(DistrictDTO dto) {
        District district = requireDistrict(dto.getDistrictId());
        String name = requireName(dto.getDistrictName(), "District name");

        if (!district.getDistrictName().equalsIgnoreCase(name)
                && districtRepository.existsByDistrictNameIgnoreCase(name)) {
            throw new Exception("A district named \"" + name + "\" already exists.");
        }

        district.setDistrictName(name);
        return toDistrictResponse(districtRepository.save(district));
    }

    @Transactional
    public Boolean deleteDistrict(String districtId) {
        District district = requireDistrict(districtId);

        boolean hasVendors = district.getWards() != null
                && district.getWards().stream()
                .anyMatch(ward -> ward.getVendors() != null && !ward.getVendors().isEmpty());

        if (hasVendors) {
            throw new Exception("Cannot delete district \"" + district.getDistrictName()
                    + "\" because one or more wards still have vendors.");
        }

        districtRepository.delete(district);
        return true;
    }

    @Transactional
    public WardResponseDTO addWard(WardDTO dto) {
        District district = requireDistrict(dto.getDistrictId());
        String name = requireName(dto.getWardName(), "Ward name");

        if (wardRepository.existsByDistrict_DistrictIdAndWardNameIgnoreCase(district.getDistrictId(), name)) {
            throw new Exception("Ward \"" + name + "\" already exists in " + district.getDistrictName() + ".");
        }

        Ward ward = new Ward();
        ward.setWardName(name);
        ward.setDistrict(district);
        return toWardResponse(wardRepository.save(ward));
    }

    @Transactional
    public WardResponseDTO updateWard(WardDTO dto) {
        Ward ward = requireWard(dto.getWardId());
        String name = requireName(dto.getWardName(), "Ward name");

        District district = ward.getDistrict();
        if (dto.getDistrictId() != null && !dto.getDistrictId().isBlank()) {
            district = requireDistrict(dto.getDistrictId());
        }

        boolean nameTaken = wardRepository
                .findByDistrict_DistrictIdAndWardNameIgnoreCase(district.getDistrictId(), name)
                .filter(existing -> !existing.getWardId().equals(ward.getWardId()))
                .isPresent();

        if (nameTaken) {
            throw new Exception("Ward \"" + name + "\" already exists in " + district.getDistrictName() + ".");
        }

        ward.setWardName(name);
        ward.setDistrict(district);
        return toWardResponse(wardRepository.save(ward));
    }

    @Transactional
    public Boolean deleteWard(String wardId) {
        Ward ward = requireWard(wardId);

        if (ward.getVendors() != null && !ward.getVendors().isEmpty()) {
            throw new Exception("Cannot delete ward \"" + ward.getWardName()
                    + "\" because it still has vendors assigned.");
        }

        wardRepository.delete(ward);
        return true;
    }

    private District requireDistrict(String districtId) {
        if (districtId == null || districtId.isBlank()) {
            throw new Exception("District ID is required.");
        }

        UUID id;
        try {
            id = UUID.fromString(districtId.trim());
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid district ID.");
        }

        return districtRepository.findById(id)
                .orElseThrow(() -> new Exception("District not found."));
    }

    private Ward requireWard(String wardId) {
        if (wardId == null || wardId.isBlank()) {
            throw new Exception("Ward ID is required.");
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

    private String requireName(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new Exception(label + " is required.");
        }
        return value.trim();
    }

    private DistrictResponseDTO toDistrictResponse(District district) {
        List<WardResponseDTO> wards = wardRepository
                .findByDistrict_DistrictIdOrderByWardNameAsc(district.getDistrictId())
                .stream()
                .map(this::toWardResponse)
                .toList();

        return new DistrictResponseDTO(
                district.getDistrictId().toString(),
                district.getDistrictName(),
                wards
        );
    }

    private WardResponseDTO toWardResponse(Ward ward) {
        District district = ward.getDistrict();
        return new WardResponseDTO(
                ward.getWardId().toString(),
                ward.getWardName(),
                district.getDistrictId().toString(),
                district.getDistrictName()
        );
    }
}
