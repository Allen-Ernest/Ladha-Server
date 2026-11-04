package tz.co.ladha.repository.vendorRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.vendor.Branch;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByVendorVendorId(UUID vendorId);

    long countByVendorVendorId(UUID vendorId);

    List<Branch> findByVendorVendorIdAndAdministratorIsNull(UUID vendorId);
}
