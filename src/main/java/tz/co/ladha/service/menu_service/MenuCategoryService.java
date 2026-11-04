package tz.co.ladha.service.menu_service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.dto.menu_dto.MenuCategoryDTO;
import tz.co.ladha.model.menu.MenuCategory;
import tz.co.ladha.repository.menu_repository.MenuCategoryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class MenuCategoryService {
    private MenuCategoryRepository menuCategoryRepository;
    private MenuSubCategoryService menuSubCategoryService;

    public MenuCategory addMenuCategory(MenuCategoryDTO menuCategoryDTO) {
        String name = menuCategoryDTO.getCategoryName().trim();
        String description = menuCategoryDTO.getCategoryDescription().trim();

        if (menuCategoryRepository.existsByCategoryName(name)
                || menuCategoryRepository.existsByCategoryDescription(description)) {
            throw new Exception("A category with name " + name + " or description " + description + " already exists");
        }

        MenuCategory menuCategory = new MenuCategory();
        menuCategory.setCategoryName(name);
        menuCategory.setCategoryDescription(description);
        return menuCategoryRepository.save(menuCategory);
    }

    public List<MenuCategory> getAllMenuCategories() {
        menuSubCategoryService.ensureDefaultsForKnownParents();
        return menuCategoryRepository.findAll();
    }

    public MenuCategory updateMenuCategory(MenuCategoryDTO menuCategoryDTO) {
        if (menuCategoryDTO.getCategoryId() == null || menuCategoryDTO.getCategoryId().isBlank()) {
            throw new Exception("Category ID is required.");
        }

        MenuCategory existing = menuCategoryRepository.findById(menuCategoryDTO.getCategoryId().trim())
                .orElseThrow(() -> new Exception("Menu category not found."));

        String name = menuCategoryDTO.getCategoryName().trim();
        String description = menuCategoryDTO.getCategoryDescription().trim();

        if (!existing.getCategoryName().equals(name) && menuCategoryRepository.existsByCategoryName(name)) {
            throw new Exception("A category with name " + name + " already exists");
        }

        if (!existing.getCategoryDescription().equals(description)
                && menuCategoryRepository.existsByCategoryDescription(description)) {
            throw new Exception("A category with description " + description + " already exists");
        }

        existing.setCategoryName(name);
        existing.setCategoryDescription(description);
        return menuCategoryRepository.save(existing);
    }

    public Boolean deleteMenuCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new Exception("Category ID is required.");
        }

        MenuCategory existing = menuCategoryRepository.findById(categoryId.trim())
                .orElseThrow(() -> new Exception("Menu category not found."));

        menuCategoryRepository.delete(existing);
        return true;
    }
}
