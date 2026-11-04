package tz.co.ladha.repository.user_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.user.delivery.DeliveryOfficer;

import java.util.List;
import java.util.UUID;

public interface DeliveryOfficerRepository extends JpaRepository<DeliveryOfficer, UUID> {
    List<DeliveryOfficer> findByBranch_Vendor_VendorIdOrderByFirstNameAsc(UUID vendorId);

    List<DeliveryOfficer> findByBranch_BranchIdOrderByFirstNameAsc(UUID branchId);

    long countByBranch_BranchId(UUID branchId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);
}
