package tz.co.ladha.controller.menu_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tz.co.ladha.dto.menu_dto.CreateMenuDTO;
import tz.co.ladha.dto.menu_dto.DeleteMenuDTO;
import tz.co.ladha.dto.menu_dto.MenuResponseDTO;
import tz.co.ladha.dto.menu_dto.MenuSubCategoryDTO;
import tz.co.ladha.dto.menu_dto.UpdateMenuDTO;
import tz.co.ladha.model.menu.SizingMode;
import tz.co.ladha.service.menu_service.MenuService;
import tz.co.ladha.service.menu_service.MenuSubCategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@AllArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final MenuSubCategoryService menuSubCategoryService;

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<MenuResponseDTO>> listByBranch(@PathVariable String branchId) {
        return ResponseEntity.ok(menuService.listByBranch(branchId));
    }

    @GetMapping("/subcategories")
    public ResponseEntity<List<MenuSubCategoryDTO>> listSubCategories(
            @RequestParam(required = false) String categoryId
    ) {
        if (categoryId != null && !categoryId.isBlank()) {
            return ResponseEntity.ok(menuSubCategoryService.listByParent(categoryId));
        }
        return ResponseEntity.ok(menuSubCategoryService.listAll());
    }

    @PostMapping("/subcategories")
    public ResponseEntity<MenuSubCategoryDTO> createSubCategory(@RequestBody Map<String, String> body) {
        SizingMode mode = SizingMode.CATEGORICAL;
        if (body.get("sizingMode") != null) {
            mode = SizingMode.valueOf(body.get("sizingMode").trim().toUpperCase());
        }
        MenuSubCategoryDTO created = menuSubCategoryService.create(
                body.get("parentCategoryId"),
                body.get("subCategoryName"),
                body.get("description"),
                mode
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponseDTO> getMenu(@PathVariable String menuId) {
        return ResponseEntity.ok(menuService.getMenu(menuId));
    }

    @PostMapping("/add")
    public ResponseEntity<MenuResponseDTO> addMenu(@Valid @RequestBody CreateMenuDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenu(dto));
    }

    @PostMapping("/update")
    public ResponseEntity<MenuResponseDTO> updateMenu(@Valid @RequestBody UpdateMenuDTO dto) {
        return ResponseEntity.ok(menuService.updateMenu(dto));
    }

    @PostMapping("/delete")
    public ResponseEntity<Boolean> deleteMenu(@Valid @RequestBody DeleteMenuDTO dto) {
        return ResponseEntity.ok(menuService.deleteMenu(dto.getMenuId()));
    }
}
