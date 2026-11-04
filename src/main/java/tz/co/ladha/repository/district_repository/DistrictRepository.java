package tz.co.ladha.repository.district_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tz.co.ladha.model.district.District;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<District, UUID> {
    boolean existsByDistrictNameIgnoreCase(String districtName);

    Optional<District> findByDistrictNameIgnoreCase(String districtName);
}
