package tz.co.ladha.repository.menu_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tz.co.ladha.model.menu.Menu;
import tz.co.ladha.model.menu.MenuCategory;

import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, String> {
    Optional<Menu> findByCategoryName(String categoryName);
    Boolean existsByCategoryName(String categoryName);
    Boolean existsByCategoryDescription(String categoryDescription);
}
