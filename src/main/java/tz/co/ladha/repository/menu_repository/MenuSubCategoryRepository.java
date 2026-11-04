package tz.co.ladha.repository.menu_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.co.ladha.model.menu.MenuSubCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuSubCategoryRepository extends JpaRepository<MenuSubCategory, UUID> {
    List<MenuSubCategory> findByParentCategory_CategoryIdOrderBySubCategoryNameAsc(String categoryId);

    Optional<MenuSubCategory> findByParentCategory_CategoryIdAndSubCategoryNameIgnoreCase(
            String categoryId,
            String subCategoryName
    );

    boolean existsByParentCategory_CategoryIdAndSubCategoryNameIgnoreCase(String categoryId, String subCategoryName);
}
