package tz.co.ladha.service.menu_service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.menu_dto.MenuSubCategoryDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.menu.MenuCategory;
import tz.co.ladha.model.menu.MenuSubCategory;
import tz.co.ladha.model.menu.SizingMode;
import tz.co.ladha.repository.menu_repository.MenuCategoryRepository;
import tz.co.ladha.repository.menu_repository.MenuSubCategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MenuSubCategoryService {
    private final MenuSubCategoryRepository menuSubCategoryRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    public List<MenuSubCategoryDTO> listAll() {
        ensureDefaultsForKnownParents();
        return menuSubCategoryRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<MenuSubCategoryDTO> listByParent(String categoryId) {
        ensureDefaultsForKnownParents();
        return menuSubCategoryRepository
                .findByParentCategory_CategoryIdOrderBySubCategoryNameAsc(categoryId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MenuSubCategoryDTO create(String parentCategoryId, String name, String description, SizingMode sizingMode) {
        MenuCategory parent = menuCategoryRepository.findById(parentCategoryId.trim())
                .orElseThrow(() -> new Exception("Parent menu category not found."));

        String trimmedName = requireText(name, "Subcategory name");
        if (menuSubCategoryRepository.existsByParentCategory_CategoryIdAndSubCategoryNameIgnoreCase(
                parent.getCategoryId(), trimmedName)) {
            throw new Exception("Subcategory '" + trimmedName + "' already exists under this category.");
        }

        MenuSubCategory sub = new MenuSubCategory();
        sub.setParentCategory(parent);
        sub.setSubCategoryName(trimmedName);
        sub.setDescription(description == null ? "" : description.trim());
        sub.setSizingMode(sizingMode == null ? SizingMode.CATEGORICAL : sizingMode);
        return toDto(menuSubCategoryRepository.save(sub));
    }

    /**
     * Splits broad platform categories (e.g. "Bites, Juices & Coffee") into discrete subcategories
     * used when vendors add individual menu items.
     */
    @Transactional
    public void ensureDefaultsForKnownParents() {
        for (MenuCategory parent : menuCategoryRepository.findAll()) {
            seedFromParentName(parent);
        }
    }

    private void seedFromParentName(MenuCategory parent) {
        String name = parent.getCategoryName() == null ? "" : parent.getCategoryName().toLowerCase(Locale.ROOT);
        List<SeedSub> seeds = new ArrayList<>();

        if (name.contains("bite") || name.contains("juice") || name.contains("coffee")) {
            seeds.add(new SeedSub("Bites", "Samosas, mandazi, chapati and other countable bites", SizingMode.COUNTABLE));
            seeds.add(new SeedSub("Juices", "Fresh juices by cup size", SizingMode.CATEGORICAL));
            seeds.add(new SeedSub("Coffee", "Hot and iced coffee by size", SizingMode.CATEGORICAL));
        } else if (name.contains("main") || name.contains("meal") || name.contains("dish")) {
            seeds.add(new SeedSub("Mains", "Main dishes portioned by diner count", SizingMode.CATEGORICAL));
        } else if (name.contains("seafood") || name.contains("fish")) {
            seeds.add(new SeedSub("Seafood", "Fish and seafood platters", SizingMode.CATEGORICAL));
        } else if (name.contains("street")) {
            seeds.add(new SeedSub("Street Food", "Street snacks and light meals", SizingMode.COUNTABLE));
        } else if (name.contains("drink") || name.contains("beverage")) {
            seeds.add(new SeedSub("Drinks", "Beverages by cup size", SizingMode.CATEGORICAL));
        } else if (name.contains("side")) {
            seeds.add(new SeedSub("Sides", "Side portions", SizingMode.COUNTABLE));
        } else {
            // Generic parent: one subcategory matching the parent name for discrete menu tagging
            seeds.add(new SeedSub(
                    parent.getCategoryName(),
                    parent.getCategoryDescription(),
                    SizingMode.CATEGORICAL
            ));
        }

        for (SeedSub seed : seeds) {
            if (!menuSubCategoryRepository.existsByParentCategory_CategoryIdAndSubCategoryNameIgnoreCase(
                    parent.getCategoryId(), seed.name())) {
                MenuSubCategory sub = new MenuSubCategory();
                sub.setParentCategory(parent);
                sub.setSubCategoryName(seed.name());
                sub.setDescription(seed.description());
                sub.setSizingMode(seed.mode());
                menuSubCategoryRepository.save(sub);
            }
        }
    }

    public MenuSubCategory require(UUID subCategoryId) {
        return menuSubCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new Exception("Menu subcategory not found."));
    }

    private MenuSubCategoryDTO toDto(MenuSubCategory sub) {
        MenuSubCategoryDTO dto = new MenuSubCategoryDTO();
        dto.setSubCategoryId(sub.getSubCategoryId());
        dto.setSubCategoryName(sub.getSubCategoryName());
        dto.setDescription(sub.getDescription());
        dto.setSizingMode(sub.getSizingMode());
        if (sub.getParentCategory() != null) {
            dto.setParentCategoryId(sub.getParentCategory().getCategoryId());
            dto.setParentCategoryName(sub.getParentCategory().getCategoryName());
        }
        return dto;
    }

    private String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new Exception(label + " is required.");
        }
        return value.trim();
    }

    private record SeedSub(String name, String description, SizingMode mode) {}
}
