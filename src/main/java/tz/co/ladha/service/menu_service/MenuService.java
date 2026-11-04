package tz.co.ladha.service.menu_service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.co.ladha.dto.menu_dto.CreateMenuDTO;
import tz.co.ladha.dto.menu_dto.MenuResponseDTO;
import tz.co.ladha.dto.menu_dto.UpdateMenuDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.menu.*;
import tz.co.ladha.model.vendor.Branch;
import tz.co.ladha.repository.menu_repository.DayRepository;
import tz.co.ladha.repository.menu_repository.MenuCategoryRepository;
import tz.co.ladha.repository.menu_repository.MenuRepository;
import tz.co.ladha.repository.vendorRepository.BranchRepository;

import java.math.BigDecimal;
import java.util.*;

@Service
@AllArgsConstructor
public class MenuService {
    private static final List<String> CATEGORICAL_ORDER = List.of("S", "M", "L", "XL", "XXL");
    private static final Map<String, Integer> DEFAULT_SERVES = Map.of(
            "S", 1,
            "M", 2,
            "L", 3,
            "XL", 4,
            "XXL", 5
    );

    private final MenuRepository menuRepository;
    private final BranchRepository branchRepository;
    private final DayRepository dayRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuSubCategoryService menuSubCategoryService;

    public List<MenuResponseDTO> listByBranch(String branchId) {
        UUID id = parseUuid(branchId, "Branch ID");
        return menuRepository.findByBranch_BranchIdOrderByMenuNameAsc(id).stream()
                .map(this::toResponse)
                .toList();
    }

    public MenuResponseDTO getMenu(String menuId) {
        return toResponse(requireMenu(menuId));
    }

    @Transactional
    public MenuResponseDTO createMenu(CreateMenuDTO dto) {
        Branch branch = requireBranch(dto.getBranchId());
        MenuSubCategory subCategory = menuSubCategoryService.require(
                parseUuid(dto.getSubCategoryId(), "Subcategory ID")
        );

        String imageUrl = requireText(dto.getImageUrl(), "Image URL");
        String menuName = requireText(dto.getMenuName(), "Menu name");

        Menu menu = new Menu();
        menu.setMenuName(menuName);
        menu.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        menu.setBranch(branch);
        menu.setSubCategory(subCategory);
        menu.setImageUrl(imageUrl);
        menu.setAvailable(dto.getAvailable() == null || dto.getAvailable());
        menu.setDaysAvailable(resolveDays(dto.getDaysAvailable()));
        menu.setSizes(buildSizes(menu, subCategory.getSizingMode(), dto.getSizes()));

        Menu saved = menuRepository.save(menu);

        // Keep legacy CategoryMenu join in sync for platform category views
        MenuCategory parent = subCategory.getParentCategory();
        if (parent != null && parent.getMenus().stream().noneMatch(m -> m.getMenuId().equals(saved.getMenuId()))) {
            parent.getMenus().add(saved);
            menuCategoryRepository.save(parent);
        }

        return toResponse(saved);
    }

    @Transactional
    public MenuResponseDTO updateMenu(UpdateMenuDTO dto) {
        Menu menu = requireMenu(dto.getMenuId());
        MenuSubCategory subCategory = menuSubCategoryService.require(
                parseUuid(dto.getSubCategoryId(), "Subcategory ID")
        );

        menu.setMenuName(requireText(dto.getMenuName(), "Menu name"));
        menu.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        menu.setSubCategory(subCategory);
        menu.setImageUrl(requireText(dto.getImageUrl(), "Image URL"));
        menu.setAvailable(dto.getAvailable() == null || dto.getAvailable());
        menu.setDaysAvailable(resolveDays(dto.getDaysAvailable()));

        menu.getSizes().clear();
        menu.getSizes().addAll(buildSizes(menu, subCategory.getSizingMode(), dto.getSizes()));

        if (dto.getBranchId() != null && !dto.getBranchId().isBlank()) {
            menu.setBranch(requireBranch(dto.getBranchId()));
        }

        return toResponse(menuRepository.save(menu));
    }

    @Transactional
    public Boolean deleteMenu(String menuId) {
        Menu menu = requireMenu(menuId);
        MenuSubCategory sub = menu.getSubCategory();
        if (sub != null && sub.getParentCategory() != null) {
            MenuCategory parent = sub.getParentCategory();
            parent.getMenus().removeIf(m -> m.getMenuId().equals(menu.getMenuId()));
            menuCategoryRepository.save(parent);
        }
        menuRepository.delete(menu);
        return true;
    }

    private Menu requireMenu(String menuId) {
        return menuRepository.findById(parseUuid(menuId, "Menu ID"))
                .orElseThrow(() -> new Exception("Menu not found."));
    }

    private List<MenuSize> buildSizes(Menu menu, SizingMode mode, List<CreateMenuDTO.MenuSizeDTO> sizeDtos) {
        if (sizeDtos == null || sizeDtos.isEmpty()) {
            throw new Exception("Add at least one size / portion.");
        }

        List<MenuSize> sizes = new ArrayList<>();
        int order = 0;

        if (mode == SizingMode.COUNTABLE) {
            Set<Integer> seen = new HashSet<>();
            for (CreateMenuDTO.MenuSizeDTO row : sizeDtos) {
                Integer pieces = row.getPieceCount();
                if (pieces == null || pieces < 1) {
                    throw new Exception("Piece count must be at least 1 for countable items.");
                }
                if (!seen.add(pieces)) {
                    throw new Exception("Duplicate piece count: " + pieces);
                }
                MenuSize size = new MenuSize();
                size.setMenu(menu);
                size.setPieceCount(pieces);
                size.setLabel(pieces == 1 ? "1 piece" : pieces + " pieces");
                size.setPrice(requirePrice(row.getPrice()));
                size.setSortOrder(order++);
                sizes.add(size);
            }
        } else {
            Set<String> seen = new HashSet<>();
            for (CreateMenuDTO.MenuSizeDTO row : sizeDtos) {
                String code = requireText(row.getSizeCode(), "Size code").toUpperCase(Locale.ROOT);
                if (!DEFAULT_SERVES.containsKey(code)) {
                    throw new Exception("Size code must be one of S, M, L, XL, XXL.");
                }
                if (!seen.add(code)) {
                    throw new Exception("Duplicate size: " + code);
                }
                int serves = row.getServesPeople() != null && row.getServesPeople() > 0
                        ? row.getServesPeople()
                        : DEFAULT_SERVES.get(code);

                MenuSize size = new MenuSize();
                size.setMenu(menu);
                size.setSizeCode(code);
                size.setServesPeople(serves);
                size.setLabel(code + " (" + serves + (serves == 1 ? " person)" : " people)"));
                size.setPrice(requirePrice(row.getPrice()));
                size.setSortOrder(CATEGORICAL_ORDER.indexOf(code));
                sizes.add(size);
            }
            sizes.sort(Comparator.comparingInt(MenuSize::getSortOrder));
            for (int i = 0; i < sizes.size(); i++) {
                sizes.get(i).setSortOrder(i);
            }
        }

        return sizes;
    }

    private List<Day> resolveDays(List<String> dayNames) {
        if (dayNames == null || dayNames.isEmpty()) {
            throw new Exception("Select at least one day of the week.");
        }
        List<Day> days = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String raw : dayNames) {
            String name = requireText(raw, "Day name");
            String key = name.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                continue;
            }
            Day day = dayRepository.findByDayNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Day created = new Day();
                        created.setDayName(capitalizeDay(name));
                        return dayRepository.save(created);
                    });
            days.add(day);
        }
        return days;
    }

    private Branch requireBranch(String branchId) {
        String raw = requireText(branchId, "Branch ID");
        UUID id;
        try {
            id = UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new Exception("Invalid branch ID.");
        }
        return branchRepository.findById(id)
                .orElseThrow(() -> new Exception("Branch not found."));
    }

    private MenuResponseDTO toResponse(Menu menu) {
        MenuResponseDTO dto = new MenuResponseDTO();
        dto.setMenuId(menu.getMenuId());
        dto.setMenuName(menu.getMenuName());
        dto.setDescription(menu.getDescription());
        dto.setImageUrl(menu.getImageUrl());
        dto.setAvailable(menu.getAvailable());

        if (menu.getBranch() != null) {
            dto.setBranchId(menu.getBranch().getBranchId());
            dto.setBranchName(menu.getBranch().getBranchName());
        }

        MenuSubCategory sub = menu.getSubCategory();
        if (sub != null) {
            dto.setSubCategoryId(sub.getSubCategoryId());
            dto.setSubCategoryName(sub.getSubCategoryName());
            dto.setSizingMode(sub.getSizingMode());
            if (sub.getParentCategory() != null) {
                dto.setParentCategoryId(sub.getParentCategory().getCategoryId());
                dto.setParentCategoryName(sub.getParentCategory().getCategoryName());
            }
        }

        dto.setDaysAvailable(
                menu.getDaysAvailable().stream()
                        .map(Day::getDayName)
                        .filter(Objects::nonNull)
                        .toList()
        );

        List<MenuResponseDTO.SizeResponse> sizes = new ArrayList<>();
        for (MenuSize size : menu.getSizes()) {
            MenuResponseDTO.SizeResponse row = new MenuResponseDTO.SizeResponse();
            row.setSizeId(size.getSizeId());
            row.setLabel(size.getLabel());
            row.setPieceCount(size.getPieceCount());
            row.setSizeCode(size.getSizeCode());
            row.setServesPeople(size.getServesPeople());
            row.setPrice(size.getPrice());
            row.setSortOrder(size.getSortOrder());
            sizes.add(row);
        }
        dto.setSizes(sizes);
        return dto;
    }

    private BigDecimal requirePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Each size must have a price greater than zero.");
        }
        return price;
    }

    private UUID parseUuid(String value, String label) {
        try {
            return UUID.fromString(requireText(value, label));
        } catch (IllegalArgumentException ex) {
            throw new Exception(label + " is invalid.");
        }
    }

    private String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new Exception(label + " is required.");
        }
        return value.trim();
    }

    private String capitalizeDay(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
