package tz.co.ladha.repository.vendorRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tz.co.ladha.model.vendor.VendorRequest;

@Repository
public interface VendorRequestRepository extends JpaRepository<VendorRequest, String> {
}
