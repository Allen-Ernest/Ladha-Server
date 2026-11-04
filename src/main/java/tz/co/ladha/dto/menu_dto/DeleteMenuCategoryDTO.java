package tz.co.ladha.dto.menu_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteMenuCategoryDTO {
    @NotBlank(message = "Category ID is required")
    private String categoryId;
}
