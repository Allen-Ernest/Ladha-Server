package tz.co.ladha.repository.district_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tz.co.ladha.model.district.Ward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID> {
    List<Ward> findByDistrict_DistrictIdOrderByWardNameAsc(UUID districtId);

    boolean existsByDistrict_DistrictIdAndWardNameIgnoreCase(UUID districtId, String wardName);

    Optional<Ward> findByDistrict_DistrictIdAndWardNameIgnoreCase(UUID districtId, String wardName);

    long countByDistrict_DistrictId(UUID districtId);
}
