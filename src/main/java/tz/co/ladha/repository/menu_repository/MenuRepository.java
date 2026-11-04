package tz.co.ladha.repository.menu_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.menu.Menu;

import java.util.List;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findByBranch_BranchIdOrderByMenuNameAsc(UUID branchId);

    long countByBranch_BranchId(UUID branchId);
}
