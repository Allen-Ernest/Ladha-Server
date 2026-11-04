package tz.co.ladha.repository.vendorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tz.co.ladha.model.vendor.Vendor;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    boolean existsByRegistrationNumber(int registrationNumber);

    boolean existsByVendorEmail(String email);

    boolean existsByVendorEmailIgnoreCase(String email);

    boolean existsByVendorPhone(String phone);

    @Query("""
            SELECT v FROM Vendor v
            WHERE (:active IS NULL OR v.active = :active)
              AND (
                   :q IS NULL OR :q = ''
                   OR LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(v.vendorEmail) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(v.vendorPhone) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR CONCAT(v.registrationNumber, '') LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<Vendor> findFiltered(
            @Param("active") Boolean active,
            @Param("q") String q,
            Pageable pageable
    );
    Optional<Vendor> findByAdministrator_UserId(UUID administratorId);
}
