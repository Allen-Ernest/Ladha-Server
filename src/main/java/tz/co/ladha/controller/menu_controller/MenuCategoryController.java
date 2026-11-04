package tz.co.ladha.controller.menu_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tz.co.ladha.dto.menu_dto.DeleteMenuCategoryDTO;
import tz.co.ladha.dto.menu_dto.MenuCategoryDTO;
import tz.co.ladha.model.menu.MenuCategory;
import tz.co.ladha.service.menu_service.MenuCategoryService;

import java.util.List;

@RestController
@RequestMapping("api/menu-cat")
@AllArgsConstructor
public class MenuCategoryController {
    @Autowired
    private MenuCategoryService menuCategoryService;

    @GetMapping("/get-menu-categories")
    public ResponseEntity<List<MenuCategory>> getMenuCategories() {
        return ResponseEntity.ok(menuCategoryService.getAllMenuCategories());
    }

    @PostMapping("/add-menu-category")
    public ResponseEntity<?> addMenuCategory(@Valid @RequestBody MenuCategoryDTO menuCategoryDTO) {
        MenuCategory menuCategory = menuCategoryService.addMenuCategory(menuCategoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuCategory);
    }

    @PostMapping("/update-menu-category")
    public ResponseEntity<?> updateMenuCategory(@Valid @RequestBody MenuCategoryDTO menuCategoryDTO) {
        MenuCategory menuCategory = menuCategoryService.updateMenuCategory(menuCategoryDTO);
        return ResponseEntity.ok(menuCategory);
    }

    @PostMapping("/delete-menu-category")
    public ResponseEntity<?> deleteMenuCategory(@Valid @RequestBody DeleteMenuCategoryDTO deleteMenuCategoryDTO) {
        Boolean deleted = menuCategoryService.deleteMenuCategory(deleteMenuCategoryDTO.getCategoryId());
        return ResponseEntity.ok(deleted);
    }
}
