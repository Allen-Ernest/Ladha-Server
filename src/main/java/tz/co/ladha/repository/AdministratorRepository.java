package tz.co.ladha.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tz.co.ladha.model.user.admin.AdminType;
import tz.co.ladha.model.user.admin.Administrator;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, UUID> {
    Optional<Administrator> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    @Query("""
            SELECT a FROM Administrator a
            WHERE (:active IS NULL OR a.active = :active)
              AND (:adminType IS NULL OR a.adminType = :adminType)
              AND (
                   :q IS NULL OR :q = ''
                   OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.phone) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    Page<Administrator> findFiltered(
            @Param("active") Boolean active,
            @Param("adminType") AdminType adminType,
            @Param("q") String q,
            Pageable pageable
    );
}
