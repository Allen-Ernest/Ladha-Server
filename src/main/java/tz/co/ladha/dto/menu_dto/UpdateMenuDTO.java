package tz.co.ladha.dto.menu_dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMenuDTO extends CreateMenuDTO {
    @NotBlank(message = "Menu ID is required")
    private String menuId;
}
