package tz.co.ladha.repository.user_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.user.order_manager.OrderManager;

import java.util.List;
import java.util.UUID;

public interface OrderManagerRepository extends JpaRepository<OrderManager, UUID> {
    List<OrderManager> findByBranch_Vendor_VendorIdOrderByFirstNameAsc(UUID vendorId);

    List<OrderManager> findByBranch_BranchIdOrderByFirstNameAsc(UUID branchId);

    long countByBranch_BranchId(UUID branchId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);
}
