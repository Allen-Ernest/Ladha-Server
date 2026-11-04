package tz.co.ladha.dto.menu_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuCategoryDTO {
    private String categoryId;

    @NotBlank(message = "Category Name is required")
    private String categoryName;

    @NotBlank(message = "Category description is required")
    private String categoryDescription;
}
