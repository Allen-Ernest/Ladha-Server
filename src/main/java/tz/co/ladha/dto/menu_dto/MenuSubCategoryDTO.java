package tz.co.ladha.dto.menu_dto;

import lombok.Getter;
import lombok.Setter;
import tz.co.ladha.model.menu.SizingMode;

import java.util.UUID;

@Getter
@Setter
public class MenuSubCategoryDTO {
    private UUID subCategoryId;
    private String subCategoryName;
    private String description;
    private SizingMode sizingMode;
    private String parentCategoryId;
    private String parentCategoryName;
}
